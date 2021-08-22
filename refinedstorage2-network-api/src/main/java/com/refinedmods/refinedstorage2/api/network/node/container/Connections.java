package com.refinedmods.refinedstorage2.api.network.node.container;

import java.util.Collections;
import java.util.Set;

public record Connections(
        Set<NetworkNodeContainer<?>> foundEntries,
        Set<NetworkNodeContainer<?>> newEntries,
        Set<NetworkNodeContainer<?>> removedEntries
) {
    public Connections(Set<NetworkNodeContainer<?>> foundEntries, Set<NetworkNodeContainer<?>> newEntries, Set<NetworkNodeContainer<?>> removedEntries) {
        this.foundEntries = Collections.unmodifiableSet(foundEntries);
        this.newEntries = Collections.unmodifiableSet(newEntries);
        this.removedEntries = Collections.unmodifiableSet(removedEntries);
    }

    public Set<NetworkNodeContainer<?>> getFoundEntries() {
        return foundEntries;
    }

    public Set<NetworkNodeContainer<?>> getNewEntries() {
        return newEntries;
    }

    public Set<NetworkNodeContainer<?>> getRemovedEntries() {
        return removedEntries;
    }
}
