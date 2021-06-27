package com.refinedmods.refinedstorage2.core.network.host;

public class Connection {
    private final NetworkNodeHost<?> source;
    private final NetworkNodeHost<?> destination;

    public Connection(NetworkNodeHost<?> source, NetworkNodeHost<?> destination) {
        this.source = source;
        this.destination = destination;
    }

    public NetworkNodeHost<?> getSource() {
        return source;
    }

    public NetworkNodeHost<?> getDestination() {
        return destination;
    }
}
