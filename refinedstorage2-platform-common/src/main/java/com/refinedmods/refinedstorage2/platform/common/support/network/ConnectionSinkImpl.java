package com.refinedmods.refinedstorage2.platform.common.support.network;

import com.refinedmods.refinedstorage2.platform.api.support.network.ConnectionSink;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;

class ConnectionSinkImpl implements ConnectionSink {
    private final GlobalPos source;
    private final Set<Connection> connections = new HashSet<>();

    ConnectionSinkImpl(final GlobalPos source) {
        this.source = source;
    }

    Set<Connection> getConnections() {
        return connections;
    }

    @Override
    public void tryConnectInSameDimension(final BlockPos pos, final Direction incomingDirection) {
        final GlobalPos globalPos = GlobalPos.of(source.dimension(), pos);
        connections.add(new Connection(globalPos, incomingDirection));
    }

    record Connection(GlobalPos pos, Direction incomingDirection) {
    }
}
