package com.ntnu.solbrille.suffixtree;

import junit.framework.TestCase;




import java.util.*;
import java.io.File;

import com.ntnu.solbrille.utils.IntArray;
import com.ntnu.solbrille.TimeCollection;
import org.carrot2.text.suffixtrees2.GeneralizedSuffixTree;
import org.carrot2.text.suffixtrees2.BitSetNode;
import org.carrot2.text.suffixtrees2.BitSetNodeFactory;
import org.carrot2.text.suffixtrees2.ISequence;
import org.carrot2.text.suffixtrees2.SuffixTree;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.porterStemmer;


/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id $.
 */
public class SuffixTreeTestCase extends TestCase {

    

    class WordList {
        private HashMap<String,Integer> terms ;
        private List<String> invlist;

        public WordList() {
            terms = new HashMap<String,Integer>();
            invlist = new ArrayList<String>();
        }

        public int getValue(String s) {
            if(!terms.containsKey(s)) {
                terms.put(s,invlist.size());
                invlist.add(s);
                return invlist.size()-1;
            }
            return terms.get(s);
        }

        public String getString(int i) {
            return invlist.get(i);
        }


        public String getWords(int list[]) {
            StringBuilder sb = new StringBuilder();
            for(int i:list) {
                sb.append(getString(i)+" ");
            }
            return sb.toString();
        }

    }

    class WordSequence implements ISequence {

        private List<Integer> words;
        private WordList wl;


        public WordSequence(WordList wl,String s) {
            words = new IntArray();
            StringTokenizer st = new StringTokenizer(s);
            this.wl = wl;
            while(st.hasMoreElements()) {
                String currWord = st.nextToken();
                words.add(wl.getValue(currWord));
            }
        }

        public int size() {
            return words.size();
        }

        public int objectAt(int i) {
            return words.get(i);
        }


    }

    //Use tf-idf of the last maxlength nodes
    public static double scoreNode(BitSetNode bsn,SuffixTree<BitSetNode> tree,WordList wl,int maxlength,Map<String,Double> termcount) {


        int num = bsn.bitset.cardinality();
        double score;
        if(num <= 1) {
           score =  0.01;
        } else if(num < 6) {
           score = num;
        } else {
           score = 6;
        }

        int start = bsn.getSuffixStartIndex();
        int stop = bsn.getSuffixEndIndex();
        if(stop -start > maxlength) {
           stop = start + maxlength;
        }

        double sum = 0.0;
        //Find most special term
        for(int i = start;i<stop;i++) {
            String term = wl.getString(tree.getInput().objectAt(i));
            Double d = termcount.get(term);
            if(d == null) {
                sum += Math.log(1/5);
            } else {
                sum += Math.log(1/d);
            }
        }
        return score*sum;

    }

    public String dumpNode(BitSetNode node, SuffixTree tree,WordList wl) {
        ISequence seqs = tree.getSequenceToRoot(node);


        StringBuilder sb = new StringBuilder();
        sb.append("\"");

        for(int i = 0;i<seqs.size();i++) {
            int pos = seqs.objectAt(i);
            if(pos < 0) {

            } else {
                sb.append(wl.getString(pos) + " ");
            }
        }
        sb.append("\" ");
        sb.append(node.bitset.cardinality());

        return sb.toString();
    }

    class BitSetNodeComparator implements Comparator<BitSetNode> {
        SuffixTree<BitSetNode> tree;
        WordList wl;
        int maxlength;
        Map<String,Double> termcount;

        public BitSetNodeComparator(SuffixTree<BitSetNode> tree,WordList wl,int maxlength,Map<String,Double> termcount) {
            this.tree = tree;
            this.wl = wl;
            this.maxlength = maxlength;
            this.termcount = termcount;

        }

        public int compare(BitSetNode a, BitSetNode b) {
            return Double.compare(scoreNode(a,tree,wl,maxlength, termcount),scoreNode(b,tree,wl,maxlength, termcount));
        }
    }

    public void testSuffix() {

        final TimeCollection tc = new TimeCollection();

        String origStrings[] = tc.getTimeCollection(new File("time"),1000);

        HashMap<String,Double> termcount = new HashMap<String,Double>();

        SnowballStemmer stemmer = new porterStemmer();

        WordSequence sequences[] = new WordSequence[origStrings.length];
        final WordList wl = new WordList();
        int nwords = 0;
        for(int i = 0;i<sequences.length;i++) {
            sequences[i] = new WordSequence(wl,origStrings[i]);
            StringTokenizer st = new StringTokenizer(origStrings[i]);
            while(st.hasMoreTokens()) {
                String term = st.nextToken();
                stemmer.setCurrent(term);
                stemmer.stem();
                term = stemmer.getCurrent();

                if(!termcount.containsKey(term)) {
                    termcount.put(term,0.0);
                }
                termcount.put(term,termcount.get(term)+1);
                nwords++;
            }
        }

        for(String term : termcount.keySet()) {
            termcount.put(term,termcount.get(term)/nwords);
        }

        final GeneralizedSuffixTree tree = new GeneralizedSuffixTree<BitSetNode>(new BitSetNodeFactory());
        System.out.println("Building the tree");
        BitSetNode root = tree.build(sequences);
        System.out.println("Beginning the sorting");

        Iterator iterator = tree.iterator();
        //Get the 500 highest scoring nodes

        
        Iterator<BitSetNode> nodeiterator = (Iterator<BitSetNode>) tree.iterator();
        List<BitSetNode> nodes = new ArrayList<BitSetNode>();

        final HashMap<BitSetNode,Double> nodescores = new HashMap<BitSetNode,Double>();

        while(nodeiterator.hasNext()) {
            BitSetNode node = nodeiterator.next();
            nodes.add(node);
            nodescores.put(node,scoreNode(node,tree,wl,6,termcount));
        }

        Collections.sort(nodes,new Comparator<BitSetNode>() {
            public int compare(BitSetNode a, BitSetNode b) {
                return nodescores.get(b).compareTo(nodescores.get(a));
            }
        });


        List<BitSetNode> topNodes = nodes.subList(0,Math.min(500,nodes.size()));


        int outputlen = Math.min(nodes.size(),100);
        for(BitSetNode topNode:topNodes.subList(0,outputlen)) {
            //System.out.println(dumpNode(topNode,tree,wl) + " " + nodescores.get(topNode));
        }

        class Cluster {
            BitSet docs;
            Set<BitSetNode> nodes;

            public double getScore() {
                double score = 0;
                for(BitSetNode node:nodes) {
                    score += nodescores.get(node);
                }
                return score;
            }

            public String getDocNames() {
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                for(int i = 0;i<docs.size();i++) {
                    if(docs.get(i)) {
                        sb.append(tc.filenames[i]+ " ");
                    }
                }
                sb.append("]");
                return sb.toString();
            }

            public String toString() {
                StringBuilder sb = new StringBuilder();
                for(BitSetNode node:nodes) {
                    sb.append("(" + dumpNode(node,tree,wl) + " " + nodescores.get(node) + ")");
                }
                return sb.toString();

            }

        }

        ArrayList<Cluster> clusters = new ArrayList<Cluster>();

        for(BitSetNode topNode:topNodes)  {
            Cluster cluster = new Cluster();
            cluster.nodes = new HashSet<BitSetNode>();
            cluster.nodes.add(topNode);
            cluster.docs = (BitSet)topNode.bitset.clone();
            clusters.add(cluster);
        }

        for(int i = 0;i<clusters.size();i++) {
            Cluster cluster = clusters.get(i);
            for(int j = i+1;j<clusters.size();j++) {
                Cluster mergeCluster = clusters.get(j);
                int n = Math.max(cluster.docs.cardinality(),mergeCluster.docs.cardinality());
                BitSet clonedBits = (BitSet)cluster.docs.clone();
                clonedBits.and(mergeCluster.docs);
                if(clonedBits.cardinality()*1.0/n > 0.5) {
                    //Do not merge if the one of the nodes is a subset of the other
                    boolean foundsubset = false;
                    Iterator<BitSetNode> bsiterator = cluster.nodes.iterator();
                    while(bsiterator.hasNext()) {
                        BitSetNode node = bsiterator.next();
                        clonedBits = (BitSet)node.bitset.clone();
                        clonedBits.xor(mergeCluster.docs);
                        //Check if one is a subset of the other
                        if(clonedBits.isEmpty()) {
                            //Figure out which we should remove
                            clonedBits = (BitSet)node.bitset.clone();
                            clonedBits.and(mergeCluster.docs);
                            if(mergeCluster.docs.cardinality() > clonedBits.cardinality()
                                 ) {
                                bsiterator.remove();
                            } else {
                                foundsubset = true;
                            }
                        }
                    }

                    //merge the two clusters
                    if(!foundsubset) {
                        cluster.docs.or(mergeCluster.docs);
                        cluster.nodes.addAll(mergeCluster.nodes);
                    }
                    clusters.remove(j);
                    j--;
                };
            }
        }

        //Now order the clusters
        Comparator<Cluster> cluscomp= new Comparator<Cluster>() {

            public int compare(Cluster a, Cluster b) {
                return new Double(b.getScore()).compareTo(a.getScore());
            }
        };

        Collections.sort(clusters,cluscomp);

        for(int i =0;i<Math.min(100,clusters.size());i++) {
            System.out.println(clusters.get(i) + ":" + clusters.get(i).getDocNames());
        }





    }
}
