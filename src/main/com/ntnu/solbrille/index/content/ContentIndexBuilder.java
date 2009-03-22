package com.ntnu.solbrille.index.content;

import com.ntnu.solbrille.utils.AbstractLifecycleComponent;

import java.io.IOException;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class ContentIndexBuilder extends AbstractLifecycleComponent {

    private final ContentIndex index;

    public ContentIndexBuilder(ContentIndex index) {
        this.index = index;
    }

    public void addDocument(long documentId, Iterable<String> content) throws IOException, InterruptedException {
        // TODO: allow overwrites etc..
        this.index.addDocument(documentId, content);
    }

    @Override
    public void start() {
        setIsRunning(true);
    }

    @Override
    public void stop() {
        setIsRunning(false);
    }
}
