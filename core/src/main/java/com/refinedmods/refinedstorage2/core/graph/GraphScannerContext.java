package com.refinedmods.refinedstorage2.core.graph;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class GraphScannerContext<T, R> {
    private final Set<T> allEntries = new HashSet<>();
    private final Set<T> removedEntries;
    private final Set<T> newEntries = new HashSet<>();
    private final Queue<R> requests = new ArrayDeque<>();

    GraphScannerContext(Set<T> previousEntries) {
        this.removedEntries = new HashSet<>(previousEntries);
    }

    public boolean addEntry(T entry) {
        if (allEntries.add(entry)) {
            boolean entryExistedPreviously = removedEntries.remove(entry);
            if (!entryExistedPreviously) {
                newEntries.add(entry);
            }

            return true;
        }

        return false;
    }

    public void addRequest(R request) {
        requests.add(request);
    }

    Queue<R> getRequests() {
        return requests;
    }

    GraphScannerResult<T> toResult() {
        return new GraphScannerResult<>(newEntries, removedEntries, allEntries);
    }
}
