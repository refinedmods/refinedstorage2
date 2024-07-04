package com.refinedmods.refinedstorage.platform.common.support.network;

import com.refinedmods.refinedstorage.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage.platform.api.support.network.ConnectionLogic;
import com.refinedmods.refinedstorage.platform.api.support.network.ConnectionSink;
import com.refinedmods.refinedstorage.platform.api.support.network.InWorldNetworkNodeContainer;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static java.util.Objects.requireNonNull;

public class InWorldNetworkNodeContainerImpl implements InWorldNetworkNodeContainer {
    private final BlockEntity blockEntity;
    private final NetworkNode node;
    private final String name;
    private final int priority;
    private final ConnectionLogic connectionLogic;
    @Nullable
    private final Supplier<Object> keyProvider;

    public InWorldNetworkNodeContainerImpl(final BlockEntity blockEntity,
                                           final NetworkNode node,
                                           final String name,
                                           final int priority,
                                           final ConnectionLogic connectionLogic,
                                           @Nullable final Supplier<Object> keyProvider) {
        this.blockEntity = blockEntity;
        this.node = node;
        this.name = name;
        this.priority = priority;
        this.connectionLogic = connectionLogic;
        this.keyProvider = keyProvider;
    }

    @Override
    public NetworkNode getNode() {
        return node;
    }

    @Override
    public void addOutgoingConnections(final ConnectionSink sink) {
        connectionLogic.addOutgoingConnections(sink);
    }

    @Override
    public boolean canAcceptIncomingConnection(final Direction incomingDirection, final BlockState connectingState) {
        return connectionLogic.canAcceptIncomingConnection(incomingDirection, connectingState);
    }

    @Override
    public BlockState getBlockState() {
        return blockEntity.getBlockState();
    }

    @Override
    public boolean isRemoved() {
        return blockEntity.isRemoved();
    }

    @Override
    public GlobalPos getPosition() {
        return GlobalPos.of(requireNonNull(blockEntity.getLevel()).dimension(), blockEntity.getBlockPos());
    }

    @Override
    public BlockPos getLocalPosition() {
        return blockEntity.getBlockPos();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Nullable
    @Override
    public Object createKey() {
        return keyProvider != null ? keyProvider.get() : null;
    }
}
