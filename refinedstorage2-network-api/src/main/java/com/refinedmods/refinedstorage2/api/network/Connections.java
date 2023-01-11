package com.refinedmods.refinedstorage2.api.network;

import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import java.util.Collections;
import java.util.Set;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public record Connections(
    Set<NetworkNodeContainer> foundEntries,
    Set<NetworkNodeContainer> newEntries,
    Set<NetworkNodeContainer> removedEntries
) {
    public Connections(final Set<NetworkNodeContainer> foundEntries,
                       final Set<NetworkNodeContainer> newEntries,
                       final Set<NetworkNodeContainer> removedEntries) {
        this.foundEntries = Collections.unmodifiableSet(foundEntries);
        this.newEntries = Collections.unmodifiableSet(newEntries);
        this.removedEntries = Collections.unmodifiableSet(removedEntries);
    }
}
