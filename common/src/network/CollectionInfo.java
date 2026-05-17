package network;

import java.io.Serializable;
import java.time.LocalDate;

/** Structured collection metadata for localized GUI rendering. */
public class CollectionInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String type;
    private final LocalDate creationDate;
    private final int size;

    public CollectionInfo(String type, LocalDate creationDate, int size) {
        this.type = type;
        this.creationDate = creationDate;
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public int getSize() {
        return size;
    }
}
