package com.ntnu.solbrille.system.test;

import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.console.SearchEngineMaster;
import com.ntnu.solbrille.query.QueryResult;
import junit.framework.TestCase;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class TimeBenchmarksTest extends TestCase {

    private final String[] queries = {
            "KENNEDY ADMINISTRATION PRESSURE ON NGO DINH DIEM TO STOP SUPPRESSING THE BUDDHISTS .",
            "CEREMONIAL SUICIDES COMMITTED BY SOME BUDDHIST MONKS IN SOUTH VIET NAM AND WHAT THEY ARE SEEKING TO GAIN BY SUCH ACTS .",
            "PRECARIOUS TRUCE IN LAOS WHICH WAS BROUGHT UP BY BRITAIN BEFORE THE 14 NATIONS THAT AGREED ON THE TRUCE IN GENEVA LAST YEAR .",
            "LEADERS WHICH FIGURE IN DISCUSSIONS OF THE FUTURE OF THE WEST GERMAN CHANCELLORSHIP .",
            "COALITION GOVERNMENT TO BE FORMED IN ITALY BY THE LEFT-WING SOCIALISTS, THE REPUBLICANS, SOCIAL DEMOCRATS, AND CHRISTIAN DEMOCRATS .",
            "PRESIDENT DE GAULLE'S POLICY ON BRITISH ENTRY INTO THE COMMON MARKET .",
            "PROVISIONS OF THE TEST BAN TREATY .",
            "OTHER NATIONS POSSESSING U.S . POLARIS MISSILES FOR THEIR NUCLEAR SUBMARINE FLEETS .",
            "CONFERENCE ON AFRICAN UNITY TO BE HELD IN ADDIS ABABA ON MAY 22 BY THE HEADS OF STATE OF 31 INDEPENDENT AFRICAN NATIONS .",
            "EFFECTS OF THE SINO-SOVIET DISPUTE ON THE NEW NATIONS OF AFRICA AND ASIA OR ON AREAS OF FERMENT IN LATIN AMERICA ."
    };

    private final int[][] relevant = {
            {268, 288, 304, 308, 323, 326, 334},
            {257, 268, 288, 304, 308, 323, 324, 326, 334},
            {87, 170, 185},
            {47, 56, 81, 103, 150, 183, 291},
            {22, 73, 173, 189, 219, 265, 277, 360, 396},
            {1, 20, 23, 32, 39, 47, 53, 54, 80, 93, 151, 157, 174, 202, 272, 291, 294, 348},
            {79, 306},
            {1, 47, 54, 89, 135, 157, 247, 254},
            {168, 227, 230, 236, 338},
            {27, 68, 78, 170, 185, 275, 279, 280, 290, 306, 307, 315, 341, 343, 401, 413, 419}
    };
    private BufferPool indexPool;
    private BufferPool contentPool;
    private SearchEngineMaster master;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        indexPool = new BufferPool(100, 1024);
        contentPool = new BufferPool(50, 1024);

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

        master = new SearchEngineMaster(indexPool, contentPool,
                dictionaryFileNumber, inv1FileNumber, inv2FileNumber,
                sysinfoFileNumber, idMappingNumber, statisticsFileNumber,
                contentIndexFileNumber, contentIndexDataFileNumber);
        master.start();
    }

    public void testBenchmark() throws InterruptedException {
        master.feedTime(new File("time/"));
        Thread.sleep(60 * 1000);
        master.flush();

        int i = 0;
        for (String query : queries) {
            System.out.println(i + ": Query: " + query);
            int[] rel = relevant[i++];
            QueryResult[] res = master.query(query, 0, rel.length * 2);
            int relevantFound = 0;
            for (int j = 0; j < res.length; j++) {
                if (isRelevant(res[j], rel)) {
                    relevantFound++;
                }
                System.out.println(j + " relevant: " + relevantFound);
            }
        }
    }

    private boolean isRelevant(QueryResult re, int[] rel) {
        String fileName = re.getStatisticsEntry().getURI().getPath();
        return false;
    }
}
