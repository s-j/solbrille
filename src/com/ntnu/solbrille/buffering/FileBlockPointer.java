package com.ntnu.solbrille.buffering;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class FileBlockPointer {
    private final int fileId;
    private final long segment;

    public FileBlockPointer(int fileId, long segment) {
        this.fileId = fileId;
        this.segment = segment;
    }

    public int getFileNumber() {
        return fileId;
    }

    public long getSegment() {
        return segment;
    }

    @Override
    public int hashCode() {
        return fileId ^ (int) (segment % (long) Integer.MAX_VALUE);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof FileBlockPointer) {
            return equals((FileBlockPointer) obj);
        }
        return false;
    }

    public boolean equals(FileBlockPointer other) {
        return other != null && other.fileId == fileId && other.segment == segment;
    }
}
