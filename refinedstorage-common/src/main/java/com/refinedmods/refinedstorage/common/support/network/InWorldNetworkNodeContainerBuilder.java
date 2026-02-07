package com.refinedmods.refinedstorage.common.support.network;

import com.refinedmods.refinedstorage.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage.common.api.support.network.ConnectionStrategy;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;

import java.util.function.Supplier;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

public class InWorldNetworkNodeContainerBuilder implements InWorldNetworkNodeContainer.Builder {
    private final BlockEntity blockEntity;
    private final NetworkNode networkNode;

    private String name = "main";
    private int priority = 0;
    private ConnectionStrategy connectionStrategy;
    @Nullable
    private Supplier<Object> keyProvider;

    public InWorldNetworkNodeContainerBuilder(final BlockEntity blockEntity, final NetworkNode networkNode) {
        this.blockEntity = blockEntity;
        this.networkNode = networkNode;
        this.connectionStrategy = new SimpleConnectionStrategy(blockEntity.getBlockPos());
    }

    @Override
    public InWorldNetworkNodeContainer.Builder name(final String builderName) {
        this.name = builderName;
        return this;
    }

    @Override
    public InWorldNetworkNodeContainer.Builder priority(final int builderPriority) {
        this.priority = builderPriority;
        return this;
    }

    @Override
    public InWorldNetworkNodeContainer.Builder connectionStrategy(final ConnectionStrategy builderConnectionStrategy) {
        this.connectionStrategy = builderConnectionStrategy;
        return this;
    }

    @Override
    public InWorldNetworkNodeContainer.Builder keyProvider(final Supplier<Object> builderKeyProvider) {
        this.keyProvider = builderKeyProvider;
        return this;
    }

    @Override
    public InWorldNetworkNodeContainer build() {
        return new InWorldNetworkNodeContainerImpl(
            blockEntity,
            networkNode,
            name,
            priority,
            connectionStrategy,
            keyProvider
        );
    }
}
