package com.ntnu.solbrille.index.occurence.test;

import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.index.occurence.DictionaryTerm;
import com.ntnu.solbrille.index.occurence.DocumentOccurence;
import com.ntnu.solbrille.index.occurence.InvertedListBuilder;
import com.ntnu.solbrille.index.occurence.InvertedListPointer;
import com.ntnu.solbrille.index.occurence.InvertedListReader;
import com.ntnu.solbrille.utils.Pair;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class InvertedListReadingWritingTest extends TestCase {

    private static final String[] terms = {"bar", "car", "tsar", "very", "world", "xylofon"};
    private static final int[][][] termDocOcc = {
            new int[][]{ // bar occurenses
                    new int[]{11, 89, 90, 76}, // doc 0
                    new int[]{12, 200}, // doc 1
                    new int[0], // doc 2
                    new int[]{6}, // doc 3
            },
            new int[][]{ // car occurenses
                    new int[]{7}, // doc 0
                    new int[0], // doc 1
                    new int[0], // doc 2
                    new int[]{7, 90, 111, 713, 900, 1100, 1200, 1500, 1800, 1900, 11111, 222222, 222233, 999999}, // doc 3
            },
            new int[][]{ // tsar occurenses
                    new int[0],
                    new int[0],
                    new int[0],
                    new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26,
                            27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50,
                            51, 52, 53, 53, 55, 56, 57, 58, 59, 60} // crazy document on the russian revolutions :P
            },
            new int[][]{ // very occurenses
                    new int[]{1}, // doc 0
                    new int[]{22, 23}, // doc 1
                    new int[]{2},// doc 2
                    new int[]{89} // doc 3
            },
            new int[][]{  // world occurenses
                    new int[]{54}, // doc 0
                    new int[0], // doc 1
                    new int[0], // doc 2
                    new int[0], // doc 3
            },
            new int[][]{ // xylofon occurenses
                    new int[0], // doc 0
                    new int[0], // doc 1
                    new int[]{25678}, // doc 2
                    new int[0], // doc 3
            },
    };

    public void testWriteThenRead() throws IOException, InterruptedException {
        BufferPool pool = new BufferPool(10, 64); // realy small buffers just to be naughty :P
        File file = new File("test.bin");
        file.createNewFile();
        FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
        try {
            int fileNumber = pool.registerFile(channel, file);
            InvertedListBuilder builder = new InvertedListBuilder(pool, fileNumber, 0);
            Pair<Map<Integer, InvertedListPointer>, Map<DictionaryTerm, InvertedListPointer>> pair = buildInvertedFile(builder, terms, termDocOcc);
            Map<Integer, InvertedListPointer> pointers = pair.getFirst();
            Map<DictionaryTerm, InvertedListPointer> termPointers = pair.getSecond();
            InvertedListReader reader = new InvertedListReader(pool, fileNumber, 0);
            for (Map.Entry<Integer, InvertedListPointer> e : pointers.entrySet()) {
                DocumentOccurence[] occurencePerDoc = new DocumentOccurence[termDocOcc[e.getKey()].length];
                for (Iterator<DocumentOccurence> it = reader.iterateTerm(new DictionaryTerm(terms[e.getKey()]), e.getValue()); it.hasNext();) {
                    DocumentOccurence occ = it.next();
                    occurencePerDoc[(int) occ.getDocumentId() - 1] = occ;
                }
                for (int i = 0; i < termDocOcc[e.getKey()].length; i++) {
                    if (termDocOcc[e.getKey()][i].length > 0) {
                        for (int j = 0; j < termDocOcc[e.getKey()][i].length; j++) {
                            assertEquals(Integer.valueOf(termDocOcc[e.getKey()][i][j]), occurencePerDoc[i].getPositionList().get(j));
                        }
                    }
                }
            }

            Iterator<Pair<DictionaryTerm, InvertedListPointer>> fileIterator = reader.getFileIterator();
            while (fileIterator.hasNext()) {
                Pair<DictionaryTerm, InvertedListPointer> term = fileIterator.next();
                assertEquals(term.getSecond(), termPointers.get(term.getFirst()));
            }
        }
        finally {
            pool.stop();
            channel.close();
            file.delete();
        }
    }

    private static Pair<Map<Integer, InvertedListPointer>, Map<DictionaryTerm, InvertedListPointer>> buildInvertedFile(InvertedListBuilder builder, String[] terms, int[][][] termDocOcc) throws IOException, InterruptedException {
        Map<Integer, InvertedListPointer> termRankToPointer = new HashMap<Integer, InvertedListPointer>();
        Map<DictionaryTerm, InvertedListPointer> termToPointer = new HashMap<DictionaryTerm, InvertedListPointer>();
        for (int i = 0; i < terms.length; i++) {
            DictionaryTerm term = new DictionaryTerm(terms[i]);
            termRankToPointer.put(i, builder.nextTerm(term));
            termToPointer.put(term, termRankToPointer.get(i));
            for (int j = 0; j < termDocOcc[i].length; j++) { // iterate documents
                if (termDocOcc[i][j].length == 0) {
                    continue;
                }
                builder.nextDocument(j + 1);
                for (int pos : termDocOcc[i][j]) {
                    builder.nextOccurence(pos);
                }
            }
        }
        builder.close();
        return new Pair<Map<Integer, InvertedListPointer>, Map<DictionaryTerm, InvertedListPointer>>(termRankToPointer, termToPointer);
    }

}
