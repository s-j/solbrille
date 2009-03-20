package com.ntnu.solbrille.console;

import com.ntnu.solbrille.TimeCollection;
import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.feeder.Feeder;
import com.ntnu.solbrille.feeder.Struct;
import com.ntnu.solbrille.feeder.processors.ContentRetriever;
import com.ntnu.solbrille.feeder.processors.LinkExtractor;
import com.ntnu.solbrille.feeder.processors.PunctuationRemover;
import com.ntnu.solbrille.feeder.processors.Stemmer;
import com.ntnu.solbrille.index.IndexerOutput;
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
import com.ntnu.solbrille.query.matching.Matcher;
import com.ntnu.solbrille.query.preprocessing.QueryPreprocessor;
import com.ntnu.solbrille.query.processing.QueryProcessor;
import com.ntnu.solbrille.query.scoring.OkapiScorer;
import com.ntnu.solbrille.query.scoring.ScoreCombiner;
import com.ntnu.solbrille.query.scoring.Scorer;
import com.ntnu.solbrille.query.scoring.SingleScoreCombiner;
import com.ntnu.solbrille.utils.AbstractLifecycleComponent;
import com.ntnu.solbrille.utils.LifecycleComponent;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class SearchEngineMaster extends AbstractLifecycleComponent {

    private static final class SearchEngineFeeder extends Feeder {

        public SearchEngineFeeder(IndexerOutput output) {
            processors.add(new ContentRetriever("uri", "content"));
            processors.add(new LinkExtractor("content", "link"));
            //processors.add(new TextToHtml("content","content"));
            processors.add(new PunctuationRemover("content", "content"));
            processors.add(new Stemmer("content", "content"));
            outputs.add(output);
        }

    }

    private final AtomicLong dummyUriCounter = new AtomicLong();

    private final BufferPool pool;
    private final OccurenceIndex occurenceIndex;
    private final OccurenceIndexBuilder occurenceIndexBuilder;
    private final DocumentStatisticsIndex statisticIndex;

    private final IndexerOutput output;
    private final Feeder feeder;

    private QueryProcessor queryProcessor;
    private final Matcher matcher;

    private final LifecycleComponent[] components;

    public SearchEngineMaster(
            BufferPool pool,
            int dictionaryFileNumber,
            int invertedListFile1, int invertedListFile2,
            int systemInfoFile, int idMappingFile, int statisticsFile) {
        this.pool = pool;
        occurenceIndex = new OccurenceIndex(pool, dictionaryFileNumber, invertedListFile1, invertedListFile2);
        occurenceIndexBuilder = new OccurenceIndexBuilder(occurenceIndex);
        statisticIndex = new DocumentStatisticsIndex(pool, occurenceIndex, systemInfoFile, idMappingFile, statisticsFile);

        components = new LifecycleComponent[]{occurenceIndex, occurenceIndexBuilder, statisticIndex};

        output = new IndexerOutput(occurenceIndexBuilder, statisticIndex);
        feeder = new SearchEngineFeeder(output);

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

    public void start() {
        if (!isRunning()) {
            pool.start();
            System.out.println("Starting master!");
            for (LifecycleComponent comp : components) {
                comp.start();
            }
            setIsRunning(true);

            Scorer okapiscorer = new OkapiScorer(statisticIndex, occurenceIndex);
            //Scorer cosinescorer = new CosineScorer(statisticIndex, occurenceIndex);
            ScoreCombiner scm = new SingleScoreCombiner(okapiscorer);

            Filters fs = new Filters();
            Filter f = new NonNegativeFilter();
            fs.addFilter(f);

            scm.addSource(matcher);
            fs.addSource(scm);

            queryProcessor = new QueryProcessor(fs, new QueryPreprocessor());

            System.out.println("Master started!");
        }
    }

    public void stop() {
        if (isRunning()) {
            System.out.println("Stopping master!");
            for (LifecycleComponent comp : components) {
                comp.stop();
            }
            pool.stop();
            setIsRunning(false);
            System.out.println("Master stopped!");
        }
    }
}
