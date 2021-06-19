package com.refinedmods.refinedstorage2.core.network.host;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.Objects;

public final class NetworkNodeHostEntry<T extends NetworkNode> {
    private final NetworkNodeHost<T> host;
    private final Rs2World world;
    private final Position position;

    public static <T extends NetworkNode> NetworkNodeHostEntry<T> create(NetworkNodeHost<T> host) {
        return new NetworkNodeHostEntry<>(host, host.getHostWorld(), host.getPosition());
    }

    private NetworkNodeHostEntry(NetworkNodeHost<T> host, Rs2World world, Position position) {
        this.host = host;
        this.world = world;
        this.position = position;
    }

    public NetworkNodeHost<T> getHost() {
        return host;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetworkNodeHostEntry that = (NetworkNodeHostEntry) o;
        return Objects.equals(world, that.world) && Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, position);
    }
}
