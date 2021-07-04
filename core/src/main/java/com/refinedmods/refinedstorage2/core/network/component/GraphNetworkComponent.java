package com.refinedmods.refinedstorage2.core.network.component;

import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.core.network.node.container.NetworkNodeContainerEntry;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GraphNetworkComponent implements NetworkComponent {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Network network;
    private final Set<NetworkNodeContainerEntry<?>> containers = new HashSet<>();

    public GraphNetworkComponent(Network network) {
        this.network = network;
    }

    public Set<NetworkNodeContainerEntry<?>> getContainers() {
        return containers;
    }

    @Override
    public void onContainerAdded(NetworkNodeContainer<?> container) {
        LOGGER.info("Container added to network {} at position {}", network.hashCode(), container.getPosition());
        containers.add(NetworkNodeContainerEntry.create(container));
    }

    @Override
    public void onContainerRemoved(NetworkNodeContainer<?> container) {
        LOGGER.info("Container removed from network {} at position {}", network.hashCode(), container.getPosition());
        containers.remove(NetworkNodeContainerEntry.create(container));
    }

    @Override
    public void onNetworkRemoved() {
        LOGGER.info("Network {} is removed", network.hashCode());
    }

    @Override
    public void onNetworkSplit(Set<Network> networks) {
        LOGGER.info("Network {} has been split into {} other networks ({})", network.hashCode(), networks.size(), networks.stream().map(Network::hashCode).collect(Collectors.toList()));
    }

    @Override
    public void onNetworkMerge(Network network) {
        LOGGER.info("Network {} has merged with network {}", network.hashCode(), this.network.hashCode());
    }
}
