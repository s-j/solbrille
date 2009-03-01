package com.ntnu.solbrille.index.occurence;

import com.ntnu.solbrille.utils.IntArray;

import java.util.List;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class DocumentOccurence {
    private final long documentId;
    private final IntArray positionList = new IntArray(1);

    public DocumentOccurence(long documentId) {
        this.documentId = documentId;
    }

    public long getDocumentId() {
        return documentId;
    }

    public List<Integer> getPositionList() {
        return positionList;
    }

    public void addPosition(int position) {
        positionList.add(position);
    }
}
