package com.ntnu.solbrille.index.occurence;

import com.ntnu.solbrille.utils.IntArray;

import java.util.List;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class DocumentOccurence implements Comparable<DocumentOccurence> {
    private final long documentId;
    private final IntArray positionList;

    public DocumentOccurence(long documentId) {
        this(documentId,new IntArray(1));

    }

    public DocumentOccurence(long documentId,List<Integer> list) {
        if(list instanceof IntArray) {
            this.documentId = documentId;
            this.positionList = (IntArray)list;
        } else {
            this.documentId = documentId;
            this.positionList = new IntArray(list);
        }
    }

    public DocumentOccurence(long documentId,IntArray list) {
        this.documentId = documentId;
        this.positionList = list;
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

    public int compareTo(DocumentOccurence o) {
        return Long.valueOf(documentId).compareTo(o.documentId);
    }

    public String toString() {
        return super.toString() + " (DocId: " + documentId + " occs: " + positionList;
    }
}
