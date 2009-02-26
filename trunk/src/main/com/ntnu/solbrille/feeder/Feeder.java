package com.ntnu.solbrille.feeder;

import com.ntnu.solbrille.feeder.processors.DocumentProcessor;
import com.ntnu.solbrille.feeder.outputs.FeederOutput;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.*;
import java.net.URI;
import java.net.URL;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id $.
 */

class CallableDoc implements Callable<Struct>{

    

    Struct struct;
    List<DocumentProcessor> documentProcessors;

    public CallableDoc(Struct struct,List<DocumentProcessor> documentProcessors) {
        this.struct = struct;
        this.documentProcessors = documentProcessors;
    }

    public Struct call() throws Exception {
        for(DocumentProcessor processor: documentProcessors) {
            processor.process(struct);
        }
        return struct;
    }
}

class RunnableOutput implements Runnable {
    Struct struct;
    List<FeederOutput> outputs;

    public RunnableOutput(Struct struct,List<FeederOutput> outputs) {
        this.struct = struct;
        this.outputs = outputs;

    }

    public void run() {
        for(FeederOutput output:outputs) {
            output.put(struct);
        }
    }
}


public class Feeder {
    List<DocumentProcessor> processors = new CopyOnWriteArrayList<DocumentProcessor>();

    List<FeederOutput> outputs = new CopyOnWriteArrayList<FeederOutput>();

    private ThreadPoolExecutor docExecutor;
    private ThreadPoolExecutor outputExecutor;
    private ExecutorCompletionService<Struct> docCompService;

    private boolean doStop = false;

    private Log LOG = LogFactory.getLog(this.getClass());

    public Feeder() {
        docExecutor = new ThreadPoolExecutor(10,20,10, TimeUnit.SECONDS, new ArrayBlockingQueue(100));
        docCompService = new ExecutorCompletionService<Struct>(docExecutor);
        outputExecutor = new ThreadPoolExecutor(10,20,10,TimeUnit.SECONDS, new ArrayBlockingQueue(100));
        outputs = Collections.synchronizedList(new ArrayList<FeederOutput>());

        Runnable outputRunner = new Runnable() {
            public void run() {
                boolean interrupted = false;
                while(!doStop) {
                    if(interrupted) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {}
                    }
                    try {
                        Future<Struct> fStruct = docCompService.take();
                        Struct struct = fStruct.get();
                        outputExecutor.execute(new RunnableOutput(struct,outputs));
                    } catch (InterruptedException e) {
                        interrupted = true;
                        LOG.error("Document completion service aborted while waiting: ",e);

                    } catch (ExecutionException e) {
                        LOG.error("Exception thrown when feeding document: ",e);
                    }
                }
            }
        };
        Thread outputThread = new Thread(outputRunner);
        outputThread.start();


    }


    public void feed(Struct document) {
        docCompService.submit(new CallableDoc(document,processors));
    }



    public void feed(URI uri,String docString) {
        Struct document = new Struct();
        document.setField("uri",uri.toString());
        document.setField("content",docString);
        feed(document);
    }

    public void feed(URI uri){
        //When feeding only the url, it is assumed that a document processor will fetch the content
        Struct document = new Struct();
        document.setField("uri",uri.toString());
        feed(document);
    }


    //TODO: This is _not_ threadsafe, may return true even though there are docs in queue
    public boolean hasDocumentsInQueue() {
        return docExecutor.getActiveCount() > 0 ||
                docExecutor.getQueue().size() > 0 ||
                outputExecutor.getActiveCount() > 0 ||
                outputExecutor.getQueue().size() > 0;
    }



}
