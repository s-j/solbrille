package com.ntnu.solbrille.console;

import com.ntnu.solbrille.TimeCollection;
import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.feeder.Feeder;
import com.ntnu.solbrille.feeder.Struct;
import com.ntnu.solbrille.feeder.processors.ContentRetriever;
import com.ntnu.solbrille.feeder.processors.HtmlToText;
import com.ntnu.solbrille.feeder.processors.LinkExtractor;
import com.ntnu.solbrille.feeder.processors.PunctuationRemover;
import com.ntnu.solbrille.feeder.processors.Stemmer;
import com.ntnu.solbrille.feeder.processors.Termizer;
import com.ntnu.solbrille.feeder.processors.Tokenizer;
import com.ntnu.solbrille.index.IndexerOutput;
import com.ntnu.solbrille.index.content.ContentIndex;
import com.ntnu.solbrille.index.content.ContentIndexBuilder;
import com.ntnu.solbrille.index.content.ContentIndexDataFileIterator;
import com.ntnu.solbrille.index.content.ContentIndexOutput;
import com.ntnu.solbrille.index.document.DocumentIndexBuilder;
import com.ntnu.solbrille.index.document.DocumentStatisticsEntry;
import com.ntnu.solbrille.index.document.DocumentStatisticsIndex;
import com.ntnu.solbrille.index.occurence.DictionaryTerm;
import com.ntnu.solbrille.index.occurence.LookupResult;
import com.ntnu.solbrille.index.occurence.OccurenceIndex;
import com.ntnu.solbrille.index.occurence.OccurenceIndexBuilder;
import com.ntnu.solbrille.query.QueryResult;
import com.ntnu.solbrille.query.clustering.SuffixTree;
import com.ntnu.solbrille.query.filtering.Filter;
import com.ntnu.solbrille.query.filtering.Filters;
import com.ntnu.solbrille.query.filtering.NonNegativeFilter;
import com.ntnu.solbrille.query.filtering.PhraseFilter;
import com.ntnu.solbrille.query.matching.Matcher;
import com.ntnu.solbrille.query.preprocessing.QueryPreprocessor;
import com.ntnu.solbrille.query.processing.DynamicSnipletExtractor;
import com.ntnu.solbrille.query.processing.QueryProcessor;
import com.ntnu.solbrille.query.scoring.OkapiScorer;
import com.ntnu.solbrille.query.scoring.ScoreCombiner;
import com.ntnu.solbrille.query.scoring.Scorer;
import com.ntnu.solbrille.query.scoring.SingleScoreCombiner;
import com.ntnu.solbrille.utils.AbstractLifecycleComponent;
import com.ntnu.solbrille.utils.LifecycleComponent;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.porterStemmer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class SearchEngineMaster extends AbstractLifecycleComponent {

    private static final class SearchEngineFeeder extends Feeder {

        public SearchEngineFeeder(
                IndexerOutput output,
                DocumentStatisticsIndex statistics,
                ContentIndexOutput contentOutput) {
            processors.add(new ContentRetriever("uri", "content"));
            processors.add(new LinkExtractor("content", "link"));
            processors.add(new HtmlToText("content", "cleanedContent"));
            processors.add(new Tokenizer("cleanedContent", "tokens"));
            processors.add(new PunctuationRemover("tokens", "cleanedTokens"));
            processors.add(new Stemmer("cleanedTokens", "cleanedTokens"));
            processors.add(new Termizer("cleanedTokens", "terms"));
            outputs.add(output);
            outputs.add(contentOutput);
        }

    }

    private final AtomicLong dummyUriCounter = new AtomicLong(System.currentTimeMillis());

    private final BufferPool indexPool;
    private final BufferPool contentPool;
    private final OccurenceIndex occurenceIndex;
    private final OccurenceIndexBuilder occurenceIndexBuilder;
    private final DocumentStatisticsIndex statisticIndex;
    private final DocumentIndexBuilder statisticIndexBuilder;

    private final ContentIndex contentIndex;
    private final ContentIndexBuilder contentIndexBuilder;

    private final IndexerOutput output;
    private final ContentIndexOutput contentOutput;
    private final Feeder feeder;

    private QueryProcessor queryProcessor;
    private final Matcher matcher;

    private Set<String> stopWords;

    private final LifecycleComponent[] components;

    public SearchEngineMaster(
            BufferPool indexPool, BufferPool contentPool,
            int dictionaryFileNumber,
            int invertedListFile1, int invertedListFile2,
            int systemInfoFile, int idMappingFile, int statisticsFile,
            int contentIndexFile,
            int contentIndexDataFile, Set<String> stopWords) {
        this.indexPool = indexPool;
        this.contentPool = contentPool;
        occurenceIndex = new OccurenceIndex(indexPool, dictionaryFileNumber, invertedListFile1, invertedListFile2);
        statisticIndex = new DocumentStatisticsIndex(indexPool, occurenceIndex, systemInfoFile, idMappingFile, statisticsFile);
        statisticIndexBuilder = new DocumentIndexBuilder(statisticIndex);

        occurenceIndexBuilder = new OccurenceIndexBuilder(occurenceIndex, statisticIndexBuilder);

        contentIndex = new ContentIndex(contentPool, contentIndexFile, contentIndexDataFile);
        contentIndexBuilder = new ContentIndexBuilder(contentIndex);

        output = new IndexerOutput(occurenceIndexBuilder, statisticIndex);
        contentOutput = new ContentIndexOutput(contentIndexBuilder);

        components = new LifecycleComponent[]{
                occurenceIndex, occurenceIndexBuilder, statisticIndex,
                statisticIndexBuilder, contentIndex, contentIndexBuilder};
        feeder = new SearchEngineFeeder(output, statisticIndex, contentOutput);

        matcher = new Matcher(occurenceIndex, statisticIndex);

        this.stopWords = stopWords;
    }

    public void feed(String document) {
        Struct doc = new Struct();
        try {
            doc.setField("uri", new URI("dummydoc/" + dummyUriCounter.incrementAndGet()));
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        doc.setField("content", document);
        feeder.feed(doc);
    }

    public void feed(Struct struct) {
        feeder.feed(struct);
    }

    public void feedTime(File f) throws InterruptedException, URISyntaxException {
        TimeCollection collection = new TimeCollection();
        String[] docs = collection.getTimeCollection(f, Integer.MAX_VALUE);
        for (int i = 0; i < docs.length; i++) {
            Struct doc = new Struct();
            doc.setField("uri", new File(f.getPath(), collection.filenames[i]).toURI());
            feeder.feed(doc);
        }

        while (feeder.hasDocumentsInQueue()) {
            Thread.sleep(100);
        }
        flush();
    }

    public QueryResult[] query(String query, int offset, int hits) {
        return queryProcessor.processQuery(query, offset, hits);
    }

    public LookupResult lookup(String term) throws IOException, InterruptedException {
        return occurenceIndex.lookup(term);
    }

    public DocumentStatisticsEntry lookupStatistics(long documentId) {
        return statisticIndex.getDocumentStatistics(documentId);
    }

    public String getSniplet(URI uri, long start, int length) throws IOException, InterruptedException, URISyntaxException {
        ContentIndexDataFileIterator sniplet = contentIndex.getContent(uri, start, length);
        if (sniplet == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        while (sniplet.hasNext()) {
            sb.append(sniplet.next());
        }
        sniplet.close();
        return sb.toString();
    }

    public String getSniplet(URI uri, long start, int length, int[] positions) throws IOException, InterruptedException, URISyntaxException {
        ContentIndexDataFileIterator sniplet = contentIndex.getContent(uri, start, length);
        if (sniplet == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int counter = (int) start;
        int pos = 0;
        while (sniplet.hasNext() && pos < positions.length) {
            if (positions[pos] == counter) {
                sb.append("<span class=\"highlight\">");
                sb.append(sniplet.next());
                sb.append("</span>");
                pos++;
            } else {
                sb.append(sniplet.next());
            }
            counter++;
        }

        while (sniplet.hasNext()) {
            sb.append(sniplet.next());
        }

        sniplet.close();
        return sb.toString();
    }

    public void printStatus() {
        System.out.println("----------------");
        System.out.println("#Terms: " + statisticIndex.getTotalNumberOfDictionaryTerms());
        System.out.println("#Docs: " + statisticIndex.getTotalNumberOfDocuments());
        System.out.println("#Tokens: " + statisticIndex.getTotalNumberOfTokens());
        System.out.println("Total size: " + statisticIndex.getTotalSize());
        System.out.println("----------------");
    }

    public Iterable<DictionaryTerm> getDict() {
        return occurenceIndex.getDictionaryTerms();
    }

    public void dumpDictionary() {
        System.out.println("--------");
        for (DictionaryTerm term : occurenceIndex.getDictionaryTerms()) {
            System.out.println(term.getTerm());
        }
        System.out.println("--------");
    }

    public void flush() throws URISyntaxException {
        Struct flush = new Struct();
        flush.addField("uri", new URI("flush"));
        flush.addField("flush", "yes");
        flush.addField("content", "");
        feeder.feed(flush);
    }

    @Override
    public void start() {
        if (!isRunning()) {
            indexPool.start();
            contentPool.start();
            System.out.println("Starting master!");
            for (LifecycleComponent comp : components) {
                comp.start();
            }
            setIsRunning(true);


            Scorer okapiscorer = new OkapiScorer(statisticIndex, occurenceIndex);
            ScoreCombiner scm = new SingleScoreCombiner(okapiscorer);
            /*
            Scorer cosinescorer = new CosineScorer(statisticIndex, occurenceIndex);
            ScoreCombiner scm = new SingleScoreCombiner(cosinescorer);
            */
            Filters fs = new Filters();
            Filter nnFilter = new NonNegativeFilter();
            Filter phFilter = new PhraseFilter();
            fs.addFilter(phFilter);
            fs.addFilter(nnFilter);

            scm.addSource(matcher);
            fs.addSource(scm);

            DynamicSnipletExtractor sniplets = new DynamicSnipletExtractor(300);
            sniplets.addSource(fs);

            SuffixTree st = new SuffixTree(contentIndex, occurenceIndex, statisticIndex, 50, stopWords);
            st.addSource(sniplets);


            //queryProcessor = new QueryProcessor(st, new QueryPreprocessor(stopWords));
            queryProcessor = new QueryProcessor(st, new QueryPreprocessor());
            System.out.println("Master started!");
        }
    }

    @Override
    public void stop() {
        if (isRunning()) {
            System.out.println("Stopping master!");
            for (LifecycleComponent comp : components) {
                comp.stop();
            }
            indexPool.stop();
            contentPool.stop();
            setIsRunning(false);
            System.out.println("Master stopped!");
        }
    }

    public static SearchEngineMaster createMaster() throws IOException {
        BufferPool indexPool = new BufferPool(300, 1024);
        BufferPool contentPool = new BufferPool(100, 1024);

        File dictionaryFile = new File("dict.bin");
        if (dictionaryFile.createNewFile()) {
            System.out.println("Dictionary file created at: " + dictionaryFile.getAbsolutePath());
        }
        FileChannel dictionaryChannel = new RandomAccessFile(dictionaryFile, "rw").getChannel();
        int dictionaryFileNumber = indexPool.registerFile(dictionaryChannel, dictionaryFile);

        File inv1File = new File("inv1.bin");
        if (inv1File.createNewFile()) {
            System.out.println("Inverted list 1 created at: " + inv1File.getAbsolutePath());
        }
        FileChannel inv1Channel = new RandomAccessFile(inv1File, "rw").getChannel();
        int inv1FileNumber = indexPool.registerFile(inv1Channel, inv1File);

        File inv2File = new File("inv2.bin");
        if (inv2File.createNewFile()) {
            System.out.println("Inverted list 2 created at: " + inv2File.getAbsolutePath());
        }
        FileChannel inv2Channel = new RandomAccessFile(inv2File, "rw").getChannel();
        int inv2FileNumber = indexPool.registerFile(inv2Channel, inv2File);

        File sysinfoFile = new File("sysinfo.bin");
        if (sysinfoFile.createNewFile()) {
            System.out.println("Sysinfo created at: " + sysinfoFile.getAbsolutePath());
        }
        FileChannel sysinfoChannel = new RandomAccessFile(sysinfoFile, "rw").getChannel();
        int sysinfoFileNumber = indexPool.registerFile(sysinfoChannel, sysinfoFile);

        File idMappingFile = new File("idMapping.bin");
        if (idMappingFile.createNewFile()) {
            System.out.println("idMapping file created at: " + idMappingFile.getAbsolutePath());
        }
        FileChannel idMappingChannel = new RandomAccessFile(idMappingFile, "rw").getChannel();
        int idMappingNumber = indexPool.registerFile(idMappingChannel, idMappingFile);

        File statisticsFile = new File("statistics.bin");
        if (statisticsFile.createNewFile()) {
            System.out.println("statistics file created at: " + statisticsFile.getAbsolutePath());
        }
        FileChannel statisticsChannel = new RandomAccessFile(statisticsFile, "rw").getChannel();
        int statisticsFileNumber = indexPool.registerFile(statisticsChannel, statisticsFile);

        File contentIndexFile = new File("contentIndex.bin");
        if (contentIndexFile.createNewFile()) {
            System.out.println("Content index created at: " + contentIndexFile.getAbsolutePath());
        }
        FileChannel contentIndexChannel = new RandomAccessFile(contentIndexFile, "rw").getChannel();
        int contentIndexFileNumber = contentPool.registerFile(contentIndexChannel, contentIndexFile);

        File contentIndexDataFile = new File("contentIndexData.bin");
        if (contentIndexDataFile.createNewFile()) {
            System.out.println("Content index data file created at: " + contentIndexDataFile.getAbsolutePath());
        }
        FileChannel contentIndexDataChannel = new RandomAccessFile(contentIndexDataFile, "rw").getChannel();
        int contentIndexDataFileNumber = contentPool.registerFile(contentIndexDataChannel, contentIndexDataFile);

        File stopWordFile = new File("TIME-stopwords.txt");
        Set<String> stopWords = new HashSet<String>();
        String line;
        BufferedReader br = new BufferedReader(new FileReader(stopWordFile));
        SnowballStemmer stemmer = new porterStemmer();
        while ((line = br.readLine()) != null) {
            stemmer.setCurrent(line.toLowerCase());
            stemmer.stem();
            stopWords.add(stemmer.getCurrent());
        }

        return new SearchEngineMaster(indexPool, contentPool,
                dictionaryFileNumber, inv1FileNumber, inv2FileNumber,
                sysinfoFileNumber, idMappingNumber, statisticsFileNumber,
                contentIndexFileNumber, contentIndexDataFileNumber, stopWords);


    }


}
