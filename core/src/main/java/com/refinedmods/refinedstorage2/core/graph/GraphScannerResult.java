package com.refinedmods.refinedstorage2.core.graph;

import java.util.Collections;
import java.util.Set;

public class GraphScannerResult<T> {
    private final Set<GraphEntry<T>> newEntries;
    private final Set<GraphEntry<T>> removedEntries;
    private final Set<GraphEntry<T>> allEntries;

    public GraphScannerResult(Set<GraphEntry<T>> newEntries, Set<GraphEntry<T>> removedEntries, Set<GraphEntry<T>> allEntries) {
        this.newEntries = Collections.unmodifiableSet(newEntries);
        this.removedEntries = Collections.unmodifiableSet(removedEntries);
        this.allEntries = Collections.unmodifiableSet(allEntries);
    }

    public Set<GraphEntry<T>> getNewEntries() {
        return newEntries;
    }

    public Set<GraphEntry<T>> getRemovedEntries() {
        return removedEntries;
    }

    public Set<GraphEntry<T>> getAllEntries() {
        return allEntries;
    }
}
