package com.ntnu.solbrille.query.processing.test;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.index.occurence.InvertedListReader;
import com.ntnu.solbrille.query.filtering.Filter;
import com.ntnu.solbrille.query.filtering.Filters;
import com.ntnu.solbrille.query.filtering.NonNegativeFilter;
import com.ntnu.solbrille.query.matching.Matcher;
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
		BufferPool pool = new BufferPool(100, 6400);
	    File file = new File("test.bin");
	    file.createNewFile();
	    FileChannel channel = new RandomAccessFile(file, "r").getChannel();
	    int fileNumber = pool.registerFile(channel, file);
		InvertedListReader reader = new InvertedListReader(pool,fileNumber,0);
		
		
		Matcher qm = new Matcher(reader);
		//TODO: provide enough information to okapi scorer
		Scorer okapiscorer = new OkapiScorer();
		ScoreCombiner scm = new SingleScoreCombiner(qm, okapiscorer);
		
		Filters fs = new Filters(scm);
		Filter f = new NonNegativeFilter();
		fs.addFilter(f);
		
		//TODO: provide query preprocessors
		QueryProcessor qproc = new QueryProcessor(qm, scm, fs); 
		
		qproc.processQuery("+Pretty Vacant", 0, 100);
	}
}
