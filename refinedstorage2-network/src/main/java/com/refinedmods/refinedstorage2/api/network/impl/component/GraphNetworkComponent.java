package com.refinedmods.refinedstorage2.api.network.impl.component;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphNetworkComponent implements NetworkComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphNetworkComponent.class);

    private final Network network;
    private final Set<NetworkNodeContainer> containers = new HashSet<>();
    private final Map<Class<? extends NetworkNodeContainer>, Set<NetworkNodeContainer>> containerIndex =
        new HashMap<>();

    public GraphNetworkComponent(final Network network) {
        this.network = network;
    }

    public Set<NetworkNodeContainer> getContainers() {
        return Collections.unmodifiableSet(containers);
    }

    @SuppressWarnings("unchecked")
    public <T extends NetworkNodeContainer> Set<T> getContainers(final Class<T> clazz) {
        return (Set<T>) Collections.unmodifiableSet(containerIndex.getOrDefault(clazz, Collections.emptySet()));
    }

    @Override
    public void onContainerAdded(final NetworkNodeContainer container) {
        LOGGER.debug("Container {} added to network {}", container, network.hashCode());
        containers.add(container);
        containerIndex.computeIfAbsent(container.getClass(), k -> new HashSet<>()).add(container);
    }

    @Override
    public void onContainerRemoved(final NetworkNodeContainer container) {
        LOGGER.debug("Container {} removed from network {}", container, network.hashCode());
        containers.remove(container);
        final Class<? extends NetworkNodeContainer> indexKey = container.getClass();
        final Set<? extends NetworkNodeContainer> index = containerIndex.get(indexKey);
        if (index != null) {
            index.remove(container);
            if (index.isEmpty()) {
                containerIndex.remove(indexKey);
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
