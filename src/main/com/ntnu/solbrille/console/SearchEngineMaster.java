package com.ntnu.solbrille.console;

import com.ntnu.solbrille.TimeCollection;
import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.feeder.Feeder;
import com.ntnu.solbrille.feeder.Struct;
import com.ntnu.solbrille.feeder.processors.ContentRetriever;
import com.ntnu.solbrille.feeder.processors.CopyProcessor;
import com.ntnu.solbrille.feeder.processors.LinkExtractor;
import com.ntnu.solbrille.feeder.processors.PunctuationRemover;
import com.ntnu.solbrille.feeder.processors.Stemmer;
import com.ntnu.solbrille.feeder.processors.TextToHtml;
import com.ntnu.solbrille.index.DocumentIdGenerator;
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
import com.ntnu.solbrille.query.filtering.Filter;
import com.ntnu.solbrille.query.filtering.Filters;
import com.ntnu.solbrille.query.filtering.NonNegativeFilter;
import com.ntnu.solbrille.query.filtering.PhraseFilter;
import com.ntnu.solbrille.query.matching.Matcher;
import com.ntnu.solbrille.query.preprocessing.QueryPreprocessor;
import com.ntnu.solbrille.query.processing.DynamicSnipletExtractor;
import com.ntnu.solbrille.query.processing.QueryProcessor;
import com.ntnu.solbrille.query.scoring.CosineScorer;
import com.ntnu.solbrille.query.scoring.ScoreCombiner;
import com.ntnu.solbrille.query.scoring.Scorer;
import com.ntnu.solbrille.query.scoring.SingleScoreCombiner;
import com.ntnu.solbrille.utils.AbstractLifecycleComponent;
import com.ntnu.solbrille.utils.LifecycleComponent;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
            processors.add(new DocumentIdGenerator("uri", "documentId", statistics));
            processors.add(new ContentRetriever("uri", "content"));
            processors.add(new LinkExtractor("content", "link"));
            processors.add(new TextToHtml("content", "content"));
            processors.add(new CopyProcessor("content", "original"));
            processors.add(new PunctuationRemover("content", "content"));
            processors.add(new Stemmer("content", "content"));
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

    private final LifecycleComponent[] components;

    public SearchEngineMaster(
            BufferPool indexPool, BufferPool contentPool,
            int dictionaryFileNumber,
            int invertedListFile1, int invertedListFile2,
            int systemInfoFile, int idMappingFile, int statisticsFile,
            int contentIndexFile,
            int contentIndexDataFile) {
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
    }

    public void feed(String document) {
        Struct doc = new Struct();
        doc.setField("uri", "dummydoc/" + dummyUriCounter.incrementAndGet());
        doc.setField("content", document);
        feeder.feed(doc);
    }

    public void feedTime(File f) {
        TimeCollection collection = new TimeCollection();
        String[] docs = collection.getTimeCollection(f, Integer.MAX_VALUE);
        for (int i = 0; i < docs.length; i++) {
            Struct doc = new Struct();
            doc.setField("uri", new File(f.getPath(), collection.filenames[i]).toURI().toString());
            feeder.feed(doc);
        }

    }

    public QueryResult[] query(String query) {
        return queryProcessor.processQuery(query, 0, 100);
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
            sb.append(" ");
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

    public void dumpDictionary() {
        System.out.println("--------");
        for (DictionaryTerm term : occurenceIndex.getDictionaryTerms()) {
            System.out.println(term.getTerm());
        }
        System.out.println("--------");
    }

    public void flush() {
        if (occurenceIndexBuilder.isRunning()) {
            try {
                occurenceIndexBuilder.updateIndex();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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

            //Scorer okapiscorer = new OkapiScorer(statisticIndex, occurenceIndex);
            Scorer cosinescorer = new CosineScorer(statisticIndex, occurenceIndex);
            //ScoreCombiner scm = new SingleScoreCombiner(okapiscorer);
            ScoreCombiner scm = new SingleScoreCombiner(cosinescorer);

            Filters fs = new Filters();
            Filter nnFilter = new NonNegativeFilter();
            Filter phFilter = new PhraseFilter();
            fs.addFilter(phFilter);
            fs.addFilter(nnFilter);

            scm.addSource(matcher);
            fs.addSource(scm);

            DynamicSnipletExtractor sniplets = new DynamicSnipletExtractor(60);
            sniplets.addSource(fs);

            queryProcessor = new QueryProcessor(sniplets, new QueryPreprocessor());

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
}
