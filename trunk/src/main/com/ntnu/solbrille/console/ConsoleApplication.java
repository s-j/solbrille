package com.ntnu.solbrille.console;

import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.index.document.DocumentStatisticsEntry;
import com.ntnu.solbrille.index.occurence.DictionaryTerm;
import com.ntnu.solbrille.index.occurence.DocumentOccurence;
import com.ntnu.solbrille.index.occurence.LookupResult;
import com.ntnu.solbrille.query.QueryResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
            master.feed(argument);
        }

    }

    private static class Flush extends Action {

        @Override
        void execute(String argument) throws Exception {
            master.flush();
            System.out.println("Flushed!");
        }

    }

    private static class Lookup extends Action {

        @Override
        void execute(String argument) throws Exception {
            LookupResult occs = master.lookup(argument);
            int count = 0;
            Iterator<DocumentOccurence> it = occs.getIterator();
            while (it.hasNext()) {
                count++;
                DocumentOccurence occ = it.next();
                StringBuilder posList = new StringBuilder("[");
                for (int pos : occ.getPositionList()) {
                    posList.append(pos).append(',');
                }
                posList.append(']');
                System.out.println(occ.getDocumentId() + ": " + posList);
                DocumentStatisticsEntry docstat = master.lookupStatistics(occ.getDocumentId());
                if (docstat != null) {
                    System.out.println("Docstats, length: " + docstat.getDocumentLength() + " tokens: " + docstat.getNumberOfTokens());
                }
                System.out.println("--------------");
            }
            System.out.println("Total number of results: " + count + " metadata says: " + occs.getDocumentCount());
        }

    }

    private static class Query extends Action {

        @Override
        void execute(String argument) throws Exception {
            QueryResult[] result = master.query(argument, 0, 10);
            for (QueryResult r : result) {
                System.out.println("---------");
                for (DictionaryTerm dt : r.getTerms()) {
                    System.out.println(r.getDocumentId() + " Score: " + r.getScore() + " #occs of " + dt.getTerm() + " " + r.getOccurences(dt).getPositionList());
                }
                System.out.println("Document info: " + r.getStatisticsEntry());
                System.out.println("Sniplets: " + r.getBestWindow());
                System.out.println(master.getSniplet(r.getStatisticsEntry().getURI(), r.getBestWindow().getFirst(), r.getBestWindow().getSecond()));
                System.out.println("---------");
            }
        }

    }

    private static class Help extends Action {

        @Override
        void execute(String argument) throws Exception {
            help();
        }

    }

    private static class Stats extends Action {

        @Override
        void execute(String argument) throws Exception {
            master.printStatus();
        }

    }

    private static class DumpDict extends Action {

        @Override
        void execute(String argument) throws Exception {
            master.dumpDictionary();
        }

    }

    private static class Exit extends Action {

        @Override
        void execute(String argument) throws Exception {
            master.stop();
            throw new RuntimeException("shutdown!");
        }

    }

    private static class Restart extends Action {

        @Override
        void execute(String argument) throws Exception {
            master.restart();
        }

    }

    private static class FeedTime extends Action {

        @Override
        void execute(String argument) throws Exception {
            File f = new File("time");
            if (f.exists() && f.isDirectory()) {
                master.feedTime(f);
            } else {
                System.out.println("TIME collection not present on system. Should be placed in \"deploy/time/\".");
            }
        }

    }

    private static class DumpDocument extends Action {

        @Override
        void execute(String argument) throws Exception {
            String[] args = argument.split(" ");
            try {
                long docId = Long.parseLong(args[0]);
                System.out.println(master.getSniplet(master.lookupStatistics(docId).getURI(), Integer.parseInt(args[1]), Integer.MAX_VALUE));
            }
            catch (NumberFormatException e) {
                System.out.println(master.getSniplet(new URI(args[0]), Integer.parseInt(args[1]), Integer.MAX_VALUE));
            }
        }

    }

    private static final Map<String, Action> actionMap = new HashMap<String, Action>();

    private static SearchEngineMaster master;
    private static BufferPool indexPool;
    private static BufferPool contentPool;

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
        System.out.println("----      Feed: \"feed <string>\" to feed a string as a document.");
        System.out.println("----      Flush: \"flush\" to flush documents fed into the searchable index.");
        System.out.println("----      Lookup: \"lookup <term>\" To lookup the term in the index.");
        System.out.println("----      Query: \"query <query>\" Execute matched query (ask Simon for query language :p).");
        System.out.println("----      Feed time: \"feedtime\" Feed the time collection to the system.");
        System.out.println("----      Help: \"help\" To show this message.");
        System.out.println("----      Stats: \"stat\" To show statistics.");
        System.out.println("----      Dump dictionary: \"dump\" Dump dictionary.");
        System.out.println("----      Dump document: \"dumpDoc <docId>\" Dump the cacehed contents of a document.");
        System.out.println("----      Restart: \"restart\" To restart the search engine.");
        System.out.println("----      Exit: \"exit\" To exit this application.");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("---- Sample console application for the SOLbRille search engine");
        help();
        master = SearchEngineMaster.createMaster();
        master.start();
        actionMap.put("help", new Help());
        actionMap.put("feed", new Feed());
        actionMap.put("lookup", new Lookup());
        actionMap.put("query", new Query());
        actionMap.put("flush", new Flush());
        actionMap.put("stat", new Stats());
        actionMap.put("dump", new DumpDict());
        actionMap.put("dumpdoc", new DumpDocument());
        actionMap.put("restart", new Restart());
        actionMap.put("feedtime", new FeedTime());
        actionMap.put("exit", new Exit());

        BufferedReader inp = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (true) {
                execute(inp.readLine());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        finally {
            master.stop();
            System.exit(0);
        }
    }
}
