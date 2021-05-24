package com.refinedmods.refinedstorage2.core.network.host;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.Optional;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class FakeNetworkNodeHostRepository implements NetworkNodeHostRepository {
    private final Table<Rs2World, Position, NetworkNodeHost<?>> hosts = HashBasedTable.create();

    public static FakeNetworkNodeHostRepository of(NetworkNodeHost<?>... hosts) {
        FakeNetworkNodeHostRepository repo = new FakeNetworkNodeHostRepository();
        for (NetworkNodeHost<?> host : hosts) {
            repo.setHost(host.getHostWorld(), host.getPosition(), host);
        }
        return repo;
    }

    public void removeHost(Rs2World world, Position position) {
        hosts.remove(world, position);
    }

    public FakeNetworkNodeHostRepository setHost(Rs2World world, Position position, NetworkNodeHost<?> host) {
        hosts.put(world, position, host);
        return this;
    }

    @Override
    public <T extends NetworkNode> Optional<NetworkNodeHost<T>> getHost(Rs2World world, Position position) {
        NetworkNodeHost<?> host = hosts.get(world, position);
        return Optional.ofNullable(host == null ? null : (NetworkNodeHost<T>) host);
    }
}
