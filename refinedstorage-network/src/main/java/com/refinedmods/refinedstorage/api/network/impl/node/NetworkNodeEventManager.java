package com.refinedmods.refinedstorage.api.network.impl.node;

import com.refinedmods.refinedstorage.api.network.node.NetworkNodeListener;

import java.util.HashSet;
import java.util.Set;

/**
 * Keeps track of the {@link NetworkNodeListener}s of a single node,
 * for use by nodes that are a {@link com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetailsProvider}.
 */
public class NetworkNodeEventManager {
    private final Set<NetworkNodeListener> listeners = new HashSet<>();

    public void addListener(final NetworkNodeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(final NetworkNodeListener listener) {
        listeners.remove(listener);
    }

    public void notifyListeners(final Object event) {
        listeners.forEach(listener -> listener.notify(event));
    }

    public void notifyDetailsChanged(final long energyUsage, final boolean active) {
        if (listeners.isEmpty()) {
            return;
        }
        notifyListeners(new NetworkNodeDetailsChangedEvent(energyUsage, active));
    }
}
