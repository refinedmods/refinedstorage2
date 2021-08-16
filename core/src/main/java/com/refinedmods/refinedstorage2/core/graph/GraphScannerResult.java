package com.refinedmods.refinedstorage2.core.graph;

import java.util.Collections;
import java.util.Set;

public record GraphScannerResult<T>(Set<T> newEntries, Set<T> removedEntries,
                                    Set<T> allEntries) {
    public GraphScannerResult(Set<T> newEntries, Set<T> removedEntries, Set<T> allEntries) {
        this.newEntries = Collections.unmodifiableSet(newEntries);
        this.removedEntries = Collections.unmodifiableSet(removedEntries);
        this.allEntries = Collections.unmodifiableSet(allEntries);
    }

    public Set<T> getNewEntries() {
        return newEntries;
    }

    public Set<T> getRemovedEntries() {
        return removedEntries;
    }

    public Set<T> getAllEntries() {
        return allEntries;
    }
}
