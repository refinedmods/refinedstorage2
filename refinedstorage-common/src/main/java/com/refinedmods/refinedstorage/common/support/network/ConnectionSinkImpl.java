package com.refinedmods.refinedstorage.common.support.network;

import com.refinedmods.refinedstorage.common.api.support.network.ConnectionSink;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

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
    public void tryConnect(final GlobalPos pos, @Nullable final Class<? extends Block> allowedBlockType) {
        connections.add(new Connection(pos, null, allowedBlockType));
    }

    @Override
    public void tryConnectInSameDimension(final BlockPos pos,
                                          final Direction incomingDirection,
                                          @Nullable final Class<? extends Block> allowedBlockType) {
        final GlobalPos globalPos = GlobalPos.of(source.dimension(), pos);
        connections.add(new Connection(globalPos, incomingDirection, allowedBlockType));
    }

    record Connection(GlobalPos pos,
                      @Nullable Direction incomingDirection,
                      @Nullable Class<? extends Block> allowedBlockType) {
    }
}
