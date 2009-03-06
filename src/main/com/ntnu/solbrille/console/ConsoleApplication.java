package com.ntnu.solbrille.console;

import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.index.occurence.DocumentOccurence;
import com.ntnu.solbrille.index.occurence.OcccurenceIndexBuilder;
import com.ntnu.solbrille.index.occurence.OccurenceIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class ConsoleApplication {


    private abstract static class Action {
        abstract void execute(String argument) throws Exception;
    }

    private static class Feed extends Action {
        @Override
        void execute(String argument) throws Exception {
            long docId = docIdGenerator.incrementAndGet();
            builder.addDocument(docId, argument);
            System.out.println("Feeded with document id: " + docId);
        }
    }

    private static class Flush extends Action {
        @Override
        void execute(String argument) throws Exception {
            builder.flush();
            System.out.println("Flushed!");
        }
    }

    private static class Lookup extends Action {
        @Override
        void execute(String argument) throws Exception {
            Iterator<DocumentOccurence> occs = index.lookup(argument);
            int count = 0;
            while (occs.hasNext()) {
                count++;
                DocumentOccurence occ = occs.next();
                StringBuilder posList = new StringBuilder("[");
                for (int pos : occ.getPositionList()) {
                    posList.append(pos).append(',');
                }
                posList.append(']');
                System.out.println(occ.getDocumentId() + ": " + posList);
            }
            System.out.println("Total number of results: " + count);
        }
    }

    private static class Help extends Action {
        @Override
        void execute(String argument) throws Exception {
            help();
        }
    }

    private static class Exit extends Action {
        @Override
        void execute(String argument) throws Exception {
            pool.stopPool();
            System.exit(0);
        }
    }

    private static final Map<String, Action> actionMap = new HashMap<String, Action>();
    private static final AtomicLong docIdGenerator = new AtomicLong(0);
    private static OccurenceIndex index;
    private static OcccurenceIndexBuilder builder;
    private static BufferPool pool;

    private static void execute(String command) throws Exception {
        String[] parts = command.split(" ", 2);
        Action a = actionMap.get(parts[0].toLowerCase());
        if (a != null) {
            a.execute(parts.length > 1 ? parts[1] : "");
        } else {
            System.out.println("Not valid action: " + parts[0]);
        }
    }

    private static void help() {
        System.out.println("---- Usage <action> args ");
        System.out.println("---- Supported actions: ");
        System.out.println("----      Feed: \"feed <string>\" to feed the string as a document.");
        System.out.println("----      Flush: \"flush\" to flush documents fed into the searchable index.");
        System.out.println("----      Lookup: \"lookup <term>\" To lookup the term in the index.");
        System.out.println("----      Help: \"help\" To show this message.");
        System.out.println("----      Exit: \"exit\" To exit this application.");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("---- Sample console application for the SOLbRille search engine");
        help();
        pool = new BufferPool(10, 128); // really small buffers, just to be evil
        File dictFile = new File("dictionary.bin");
        if (dictFile.createNewFile()) {
            System.out.println("Dictionary created at: " + dictFile.getAbsolutePath());
        }
        FileChannel dictChannel = new RandomAccessFile(dictFile, "rw").getChannel();
        int dictFileNumber = pool.registerFile(dictChannel, dictFile);

        File inv1File = new File("inv1.bin");
        if (inv1File.createNewFile()) {
            System.out.println("Inverted list 1 created at: " + inv1File.getAbsolutePath());
        }
        FileChannel inv1Channel = new RandomAccessFile(inv1File, "rw").getChannel();
        int inv1FileNumber = pool.registerFile(inv1Channel, inv1File);

        File inv2File = new File("inv2.bin");
        if (inv2File.createNewFile()) {
            System.out.println("Inverted list 2 created at: " + inv1File.getAbsolutePath());
        }
        FileChannel inv2Channel = new RandomAccessFile(inv2File, "rw").getChannel();
        int inv2FileNumber = pool.registerFile(inv2Channel, inv2File);

        index = new OccurenceIndex(pool, dictFileNumber, inv1FileNumber, inv2FileNumber);
        builder = new OcccurenceIndexBuilder(index);
        actionMap.put("help", new Help());
        actionMap.put("feed", new Feed());
        actionMap.put("lookup", new Lookup());
        actionMap.put("flush", new Flush());
        actionMap.put("exit", new Exit());

        BufferedReader inp = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            execute(inp.readLine());
        }
    }
}
