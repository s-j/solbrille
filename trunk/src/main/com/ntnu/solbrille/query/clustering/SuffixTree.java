package com.ntnu.solbrille.query.clustering;

import com.ntnu.solbrille.query.processing.QueryProcessingComponent;
import com.ntnu.solbrille.query.QueryRequest;
import com.ntnu.solbrille.query.QueryResult;
import com.ntnu.solbrille.index.content.ContentIndex;
import com.ntnu.solbrille.index.content.ContentIndexDataFileIterator;
import com.ntnu.solbrille.index.occurence.OccurenceIndex;
import com.ntnu.solbrille.index.document.DocumentStatisticsIndex;
import com.ntnu.solbrille.utils.Pair;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.BitSet;
import java.util.ListIterator;
import java.util.Set;
import java.util.regex.Pattern;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carrot2.text.suffixtrees2.GeneralizedSuffixTree;
import org.carrot2.text.suffixtrees2.BitSetNode;
import org.carrot2.text.suffixtrees2.BitSetNodeFactory;
import org.carrot2.text.suffixtrees2.ISequence;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.porterStemmer;


/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id$.
 */
public class SuffixTree implements QueryProcessingComponent{

    private Log LOG = LogFactory.getLog(this.getClass());

    private final HashMap<SuffixCluster,Double> clusterScores = new HashMap<SuffixCluster,Double>();

    QueryProcessingComponent source = null;
    List<QueryResult> results;
    int pos;
    private int maxDocs;
    ContentIndex contentIndex;
    OccurenceIndex occurenceIndex;
    Map<int[],Double> labelCache = new HashMap<int[],Double>();
    private DocumentStatisticsIndex statisticsIndex;
    private Set<String> stopWords;


    public SuffixTree(ContentIndex contentIndex, OccurenceIndex occurenceIndex, DocumentStatisticsIndex statisticsIndex,int maxDocs,
                      Set<String> stopWords) {
        this.maxDocs = maxDocs;
        this.contentIndex = contentIndex;
        this.occurenceIndex = occurenceIndex;
        this.statisticsIndex = statisticsIndex;
        this.stopWords = stopWords;
    }

    @Override
    public void addSource(QueryProcessingComponent source) {
        this.source = source;
    }

    @Override
    public boolean loadQuery(QueryRequest query) {
        pos = 0;
        results = null;
        return source.loadQuery(query);
    }



    public double scoreLabel(int label[],WordList wl) {
        Double d = labelCache.get(label);
        if(d == null) {
            double termscore = 0;
            for(int i = 0;i<label.length;i++) {
                int t = label[i];
                if(t < 0) {
                    continue;
                }
                String term = wl.getString(label[i]);
                long occs = 0;
                try {
                    occs = occurenceIndex.getDocumentCount(term);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(occs != 0) {
                    termscore += Math.log(1/(occs*1.0/statisticsIndex.getTotalNumberOfDocuments()));
                }
            }
            d = termscore;
            labelCache.put(label,termscore);
        }
        return d;


    }

    public double scoreCluster(SuffixCluster cluster,WordList wl,int maxlength,List<QueryResult> results) {
        int numdocs = cluster.docs.cardinality();
        double score;
        if(numdocs <=1) {
            return 0.001;
        } else if(numdocs < maxlength) {
            score = numdocs;
        } else {
            score = maxlength;
        }
        double maxLabel=0.0;
        for(int[] label:cluster.labels) {
            maxLabel = Math.max(scoreLabel(label,wl),maxLabel);
        }
        score *= maxLabel;

        double maxScore = 0.0;
        double avgScore = 0.0;
        for(int i=cluster.docs.nextSetBit(0); i>=0; i=cluster.docs.nextSetBit(i+1)) {
                maxScore = Math.max(results.get(i).getScore(),maxScore);
                avgScore += results.get(i).getScore();
        }
        score *=avgScore*1.0/numdocs;

        return score;

    }

   /* public double scoreNode(BitSetNode bsn,
                            org.carrot2.text.suffixtrees2.SuffixTree<BitSetNode> tree,
                            WordList wl,
                            int maxlength) {
        int num = bsn.bitset.cardinality();
        double score;
        if(num == 1) {
            score = 0.001;
        } else if(num < maxlength) {
            score = num;
        } else {
            score = maxlength;
        }

        int start = bsn.getSuffixStartIndex();
        int stop = bsn.getSuffixEndIndex();
        score *= (stop-start > maxlength ? maxlength : stop-start);

        double termscore = 0.0;

        int label[] = new int[stop-start];
        for(int i = 0;i<stop-start;i++) {
            label[i] = tree.getInput().objectAt(start+i);
        }
        return score*scoreLabel(label,wl);
    }
    */

    public String dumpLabel(int label[],WordList wl) {

        StringBuilder sb = new StringBuilder();

        for(int i = 0;i<label.length;i++) {
            int pos = label[i];
            if(pos < 0) {

            } else {
                sb.append(wl.getString(pos) + " ");
            }
        }
        return sb.toString();
    }

   

    /**
     * Check if b is subset of a. Runs in n^2
     * @param a
     * @param b
     * @return
     */
    public static boolean isSubset(int a[],int b[]) {
        if(b.length > a.length)
            return false;
        for(int i = 0;i<=a.length-b.length;i++) {
            boolean isSubset = true;
            for(int j=0;j<b.length;j++) {
                if(a[i+j] != b[j]) {
                    isSubset = false;
                    break;
                }
            }
            if(isSubset)
                return true;
        }
        return false;



    }



    public void getResults() {
        //Collect up to maxDocs QueryResults
        results = new ArrayList<QueryResult>();
        labelCache.clear();
        int countDocs = 0;
        while(source.hasNext() && countDocs++ < maxDocs) {
            results.add(source.next());
        }

        if(results.size() == 0)
            return;

        Map<Integer,QueryResult> docmap = new HashMap<Integer,QueryResult>();
        WordList wl = new WordList();

        Pattern pattern = Pattern.compile("[\\p{P}]+");
        int docPos = 0;
        WordSequence sequences[] = new WordSequence[results.size()];

        SnowballStemmer stemmer = new porterStemmer();

        for(QueryResult result:results) {
            WordSequence sequence = new WordSequence(wl);

            docmap.put(docPos,result);
            //Remove punctuation

            Pair<Integer,Integer> window = result.getBestWindow();
            ContentIndexDataFileIterator contentIterator = null;
            try {
                contentIterator =  contentIndex.getContent(
                        result.getStatisticsEntry().getURI()
                        ,window.getFirst(),
                        window.getSecond());
            } catch (IOException e) {
                LOG.error(e);
                return;
            } catch (InterruptedException e) {
                LOG.error(e);
                return;
            }
            if(contentIterator == null) {
                continue;
            }

            StringBuilder sb = new StringBuilder();

            while(contentIterator.hasNext()) {
                sb.append(pattern.matcher(contentIterator.next().toLowerCase()).replaceAll(" "));
            }
            contentIterator.close();

            StringTokenizer st = new StringTokenizer(sb.toString());

            while(st.hasMoreTokens()) {
                String term = st.nextToken();
                term = term.trim();

                if(term.length() > 2 && !stopWords.contains(term))
                    sequence.addWord(term);


            }
            sequences[docPos] = sequence;
            docPos++;

        }

        //Now we are ready to create the suffix tree
        final GeneralizedSuffixTree tree = new GeneralizedSuffixTree<BitSetNode>(new BitSetNodeFactory());
        tree.build(sequences);
        Iterator<BitSetNode> nodeiterator = (Iterator<BitSetNode>) tree.iterator();
        clusterScores.clear();




        List<SuffixCluster> clusters = new ArrayList<SuffixCluster>();
        while(nodeiterator.hasNext()) {
            BitSetNode node= nodeiterator.next();
            SuffixCluster cluster = new SuffixCluster(wl);
            cluster.docs = node.bitset;

            ISequence sequence = tree.getSequenceToRoot(node);
            int start = 0;

            int label[] = new int[sequence.size()-start];
            for(int i = start;i<sequence.size();i++) {
                label[i] = sequence.objectAt(i);
            }
            cluster.labels.add(label);
            clusterScores.put(cluster,scoreCluster(cluster,wl,6,results));
            clusters.add(cluster);
        }

        Comparator<SuffixCluster> clusterComparator = new Comparator<SuffixCluster>() {
        @Override
            public int compare(SuffixCluster a, SuffixCluster b) {
                return clusterScores.get(a).compareTo(clusterScores.get(b));
            }
        };

        Collections.sort(clusters,Collections.reverseOrder(clusterComparator));

        clusters= clusters.subList(0,Math.min(clusters.size(),500));
        clusterScores.clear();

        ArrayList<SuffixCluster> mergeList = new ArrayList<SuffixCluster>();
        for(int i = 0;i<clusters.size();i++) {
            SuffixCluster cluster = clusters.get(i);
            scoreCluster(cluster,wl,6,results);
            mergeList.clear();

            Iterator<SuffixCluster> mergeClusters = clusters.listIterator(i+1);
            while(mergeClusters.hasNext()) {
                SuffixCluster mergeCluster = mergeClusters.next();
                if(shouldMerge(cluster.docs,mergeCluster.docs))  {
                    //Ok, we know that we should merge
                    mergeList.add(mergeCluster);
                    mergeClusters.remove();
                }
            }

            for(SuffixCluster mergeCluster:mergeList) {
                mergeClusters(cluster,mergeCluster);
            }
            clusterScores.put(cluster,scoreCluster(cluster,wl,6,results));
        }

        Collections.sort(clusters,Collections.reverseOrder(clusterComparator));

        //Then convert the suffixClusters to regular clusters

        ClusterList cl = new ClusterList();
        double maxScore = clusterScores.get(clusters.get(0));
        for(SuffixCluster suffixCluster:clusters) {
            if(clusterScores.get(suffixCluster) < maxScore/5) {
                break;
            }
            Cluster cluster = new Cluster();
            cluster.setScore(clusterScores.get(suffixCluster));
            for(int i=suffixCluster.docs.nextSetBit(0); i>=0; i=suffixCluster.docs.nextSetBit(i+1)) {
                cluster.getResults().add(results.get(i));
            }

            //Create tags

            for(int label[] :suffixCluster.labels) {
                cluster.getTags().add(dumpLabel(label,wl));
            }
            cl.add(cluster);
        }

        for(QueryResult result:results) {
            result.setClusterList(cl);
        }
    }

    /**
     * Merge mergeCluster into cluster
     * @param cluster
     * @param mergeCluster
     */

    private void mergeClusters(SuffixCluster cluster, SuffixCluster mergeCluster) {

        //If mergeCluster contains a label that is a subset of a subset in
        //cluster, we choose the best label of the two.
        //Well, actually we choose the label that is the outermost

        cluster.docs.or(mergeCluster.docs);

        for(int[] mergeLabel:mergeCluster.labels) {
            ListIterator<int[]> labelIterator = cluster.labels.listIterator();
            boolean foundLabel = false;
            while(labelIterator.hasNext()) {
                int label[] = labelIterator.next();
                if(isSubset(label,mergeLabel)) {
                    foundLabel = true;
                    break;
                } else if(isSubset(mergeLabel,label)) {
                    labelIterator.remove();
                }
            }
            if(!foundLabel)  {
                cluster.labels.add(mergeLabel);
            }
        }


    }

    private boolean shouldMerge(BitSet a, BitSet b) {
        //Have to copy both off
        BitSet testCopy = (BitSet)a.clone();
        testCopy.and(b);
        return testCopy.cardinality()*1.0/a.cardinality() > 0.5 &&
                testCopy.cardinality()*1.0/b.cardinality() > 0.5;
    }


    class SuffixCluster {
        BitSet docs = new BitSet();
        ArrayList<int[]> labels = new ArrayList<int[]>();
        WordList wl;

        public SuffixCluster(WordList wl) {
            this.wl = wl;
        }

        @Override
        public boolean equals(Object o) {
            if(!(o instanceof SuffixCluster))
                return false;
            SuffixCluster c = (SuffixCluster) o;

            return this.docs.equals(c.docs) &&
                    labels.equals(c.labels);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(int[] label:labels)
                sb.append("'" + dumpLabel(label,wl)+ "',");
            return sb.toString();
        }

    }


    @Override
    public boolean hasNext() {
        if(results == null) {
            getResults();
        }
        return pos < results.size();
    }

    @Override
    public QueryResult next() {
        if(results == null) {
            getResults();
        }
        return results.get(pos++);
    }

    @Override
    public void remove() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
