package com.ntnu.solbrille.query.processing.test;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.index.document.DocumentStatisticsIndex;
import com.ntnu.solbrille.index.occurence.OccurenceIndex;
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
/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public class TestQueryProcessor {
	
	
	public void testProcessing() throws Exception{
        BufferPool pool = new BufferPool(10, 128); // really small buffers, just to be evil
        File dictFile = new File("dictionary.bin");
        dictFile.createNewFile();
        FileChannel dictChannel = new RandomAccessFile(dictFile, "rw").getChannel();
        int dictFileNumber = pool.registerFile(dictChannel, dictFile);

        File inv1File = new File("inv1.bin");
        inv1File.createNewFile();
        FileChannel inv1Channel = new RandomAccessFile(inv1File, "rw").getChannel();
        int inv1FileNumber = pool.registerFile(inv1Channel, inv1File);

        File inv2File = new File("inv2.bin");
        inv1File.createNewFile();
        FileChannel inv2Channel = new RandomAccessFile(inv2File, "rw").getChannel();
        int inv2FileNumber = pool.registerFile(inv2Channel, inv2File);
        
        OccurenceIndex occurenceIndex = new OccurenceIndex(pool, dictFileNumber, inv1FileNumber, inv2FileNumber);
		DocumentStatisticsIndex statisticsIndex = new DocumentStatisticsIndex(occurenceIndex);
		
		Matcher qm = new Matcher(occurenceIndex);
		
		//TODO: provide enough information to okapi scorer
		//something like StatisticIndex
		Scorer okapiscorer = new OkapiScorer(statisticsIndex, occurenceIndex);
		ScoreCombiner scm = new SingleScoreCombiner(okapiscorer);
		
		Filters fs = new Filters();
		Filter f = new NonNegativeFilter();
		fs.addFilter(f);
		
		scm.addSource(qm);
		fs.addSource(scm);
		
		QueryPreprocessor preproc = new QueryPreprocessor();
		QueryProcessor qproc = new QueryProcessor(fs, preproc); 
		
		
		//TODO: provide query preprocessors
		qproc.processQuery("+Pretty Vacant", 0, 100);
	}
}
