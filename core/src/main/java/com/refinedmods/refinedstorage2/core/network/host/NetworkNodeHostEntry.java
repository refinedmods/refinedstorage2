package com.refinedmods.refinedstorage2.core.network.host;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.Objects;

public final class NetworkNodeHostEntry {
    private final NetworkNodeHost host;
    private final Rs2World world;
    private final Position position;

    public static NetworkNodeHostEntry create(NetworkNodeHost host) {
        return new NetworkNodeHostEntry(host, host.getHostWorld(), host.getPosition());
    }

    private NetworkNodeHostEntry(NetworkNodeHost host, Rs2World world, Position position) {
        this.host = host;
        this.world = world;
        this.position = position;
    }

    public NetworkNodeHost getHost() {
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
