package com.refinedmods.refinedstorage2.api.network.component;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GraphNetworkComponent implements NetworkComponent {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Network network;
    private final Set<NetworkNodeContainer> containers = new HashSet<>();

    public GraphNetworkComponent(Network network) {
        this.network = network;
    }

    public Set<NetworkNodeContainer> getContainers() {
        return Collections.unmodifiableSet(containers);
    }

    @Override
    public void onContainerAdded(NetworkNodeContainer container) {
        LOGGER.info("Container added to network {}", network.hashCode());
        containers.add(container);
    }

    @Override
    public void onContainerRemoved(NetworkNodeContainer container) {
        LOGGER.info("Container removed from network {}", network.hashCode());
        containers.remove(container);
    }

    @Override
    public void onNetworkRemoved() {
        LOGGER.info("Network {} is removed", network.hashCode());
    }

    @Override
    public void onNetworkSplit(Set<Network> networks) {
        LOGGER.info("Network {} has been split into {} other networks ({})", network.hashCode(), networks.size(), networks.stream().map(Network::hashCode).toList());
    }

    @Override
    public void onNetworkMerge(Network network) {
        LOGGER.info("Network {} has merged with network {}", network.hashCode(), this.network.hashCode());
    }
}
