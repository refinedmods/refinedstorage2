package com.refinedmods.refinedstorage2.core.network.node.container;

public class Connection {
    private final NetworkNodeContainer<?> source;
    private final NetworkNodeContainer<?> destination;

    public Connection(NetworkNodeContainer<?> source, NetworkNodeContainer<?> destination) {
        this.source = source;
        this.destination = destination;
    }

    public NetworkNodeContainer<?> getSource() {
        return source;
    }

    public NetworkNodeContainer<?> getDestination() {
        return destination;
    }
}
