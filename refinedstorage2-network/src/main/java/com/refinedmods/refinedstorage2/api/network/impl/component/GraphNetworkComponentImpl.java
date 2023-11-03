package com.refinedmods.refinedstorage2.api.network.impl.component;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphNetworkComponentImpl implements GraphNetworkComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphNetworkComponentImpl.class);

    private final Network network;
    private final Set<NetworkNodeContainer> containers = new HashSet<>();
    private final Map<Class<?>, Set<NetworkNodeContainer>> byClassIndex = new HashMap<>();
    private final Map<Object, NetworkNodeContainer> byKeyIndex = new HashMap<>();

    public GraphNetworkComponentImpl(final Network network) {
        this.network = network;
    }

    @Override
    public Set<NetworkNodeContainer> getContainers() {
        return Collections.unmodifiableSet(containers);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Set<T> getContainers(final Class<T> clazz) {
        return (Set<T>) Collections.unmodifiableSet(byClassIndex.getOrDefault(clazz, Collections.emptySet()));
    }

    @Override
    @Nullable
    public NetworkNodeContainer getContainer(final Object key) {
        return byKeyIndex.get(key);
    }

    @Override
    public void onContainerAdded(final NetworkNodeContainer container) {
        LOGGER.debug("Container {} added to network {}", container, network.hashCode());
        containers.add(container);
        addToIndex(container);
    }

    @Override
    public void onContainerRemoved(final NetworkNodeContainer container) {
        LOGGER.debug("Container {} removed from network {}", container, network.hashCode());
        containers.remove(container);
        removeFromIndex(container);
    }

    private void addToIndex(final NetworkNodeContainer container) {
        final Object key = container.createKey();
        if (key != null) {
            byKeyIndex.put(key, container);
        }
        final Class<? extends NetworkNodeContainer> clazz = container.getClass();
        addToIndex(clazz, container);
        for (final Class<?> iface : clazz.getInterfaces()) {
            addToIndex(iface, container);
        }
    }

    private void addToIndex(final Class<?> indexKey, final NetworkNodeContainer container) {
        byClassIndex.computeIfAbsent(indexKey, k -> new HashSet<>()).add(container);
    }

    private void removeFromIndex(final NetworkNodeContainer container) {
        final Object key = container.createKey();
        if (key != null) {
            byKeyIndex.remove(key);
        }
        final Class<? extends NetworkNodeContainer> clazz = container.getClass();
        removeFromIndex(clazz, container);
        for (final Class<?> iface : clazz.getInterfaces()) {
            removeFromIndex(iface, container);
        }
    }

    private void removeFromIndex(final Class<?> indexKey, final NetworkNodeContainer container) {
        final Set<? extends NetworkNodeContainer> index = byClassIndex.get(indexKey);
        if (index != null) {
            index.remove(container);
            if (index.isEmpty()) {
                byClassIndex.remove(indexKey);
            }
        }
    }

    @Override
    public void onNetworkRemoved() {
        LOGGER.debug("Network {} is removed", network.hashCode());
    }

    @Override
    public void onNetworkSplit(final Set<Network> networks) {
        LOGGER.debug(
            "Network {} has been split into {} other networks ({})",
            network.hashCode(),
            networks.size(),
            networks.stream().map(Network::hashCode).toList()
        );
    }

    @Override
    public void onNetworkMergedWith(final Network newMainNetwork) {
        LOGGER.debug("Network {} has merged with network {}", newMainNetwork.hashCode(), this.network.hashCode());
    }
}
