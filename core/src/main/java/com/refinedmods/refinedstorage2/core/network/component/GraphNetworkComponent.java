package com.refinedmods.refinedstorage2.core.network.component;

import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.network.host.NetworkNodeHost;
import com.refinedmods.refinedstorage2.core.network.host.NetworkNodeHostEntry;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GraphNetworkComponent implements NetworkComponent {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Network network;
    private final Set<NetworkNodeHostEntry> hosts = new HashSet<>();

    public GraphNetworkComponent(Network network) {
        this.network = network;
    }

    public Set<NetworkNodeHostEntry> getHosts() {
        return hosts;
    }

    @Override
    public void onHostAdded(NetworkNodeHost<?> host) {
        LOGGER.info("Host added to network {} at position {}", network.hashCode(), host.getPosition());
        hosts.add(NetworkNodeHostEntry.create(host));
    }

    @Override
    public void onHostRemoved(NetworkNodeHost<?> host) {
        LOGGER.info("Host removed from network {} at position {}", network.hashCode(), host.getPosition());
        hosts.remove(NetworkNodeHostEntry.create(host));
    }
}
