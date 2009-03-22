package com.ntnu.solbrille.index.content;

import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.index.BasicNavigableKeyValueIndex;
import com.ntnu.solbrille.index.document.DocumentIdEntry;
import com.ntnu.solbrille.utils.AbstractLifecycleComponent;

import java.io.IOException;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class ContentIndex extends AbstractLifecycleComponent {

    private final ContentIndexDataFile dataFile = new ContentIndexDataFile();
    private final BasicNavigableKeyValueIndex<DocumentIdEntry, ContentIndexDataFilePointer> contentIndex
            = new BasicNavigableKeyValueIndex<DocumentIdEntry, ContentIndexDataFilePointer>(
            new DocumentIdEntry.DocumentIdEntryDescriptor(),
            new ContentIndexDataFilePointer.ContentIndexDataFilePointerDescriptor()
    );

    private final BufferPool pool;
    private final int contentIndexFileNumber;
    private final int contentDataFileNumber;

    public ContentIndex(BufferPool pool, int contentIndexFileNumber, int contentDataFileNumber) {
        this.pool = pool;
        this.contentIndexFileNumber = contentIndexFileNumber;
        this.contentDataFileNumber = contentDataFileNumber;
    }

    public ContentIndexDataFileIterator getContent(long documentId, long offset, int length)
            throws IOException, InterruptedException {
        ContentIndexDataFilePointer pointer = contentIndex.get(new DocumentIdEntry(documentId));
        if (pointer != null) {
            return new ContentIndexDataFileIterator(contentDataFileNumber, pool, pointer, offset, length);
        }
        return null;
    }

    @Override
    public void start() {
        try {
            dataFile.initializeFromFile(pool, contentDataFileNumber, 0);
            contentIndex.initializeFromFile(pool, contentIndexFileNumber, 0);
        } catch (IOException e) {
            e.printStackTrace();
            setFailCause(e);
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
            setFailCause(e);
            return;
        }
        setIsRunning(true);
    }

    @Override
    public void stop() {
        try {
            dataFile.writeToFile(pool, contentDataFileNumber, 0);
            dataFile.close();
            contentIndex.writeToFile(pool, contentIndexFileNumber, 0);
        } catch (IOException e) {
            e.printStackTrace();
            setFailCause(e);
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
            setFailCause(e);
            return;
        }
        setIsRunning(false);
    }

    void addDocument(long documentId, Iterable<String> content) throws IOException, InterruptedException {
        ContentIndexDataFilePointer pointer = dataFile.writeContent(content);
        contentIndex.put(new DocumentIdEntry(documentId), pointer);
    }
}
