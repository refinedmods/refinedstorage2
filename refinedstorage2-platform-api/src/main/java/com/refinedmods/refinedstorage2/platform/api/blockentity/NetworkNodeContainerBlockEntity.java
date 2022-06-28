package com.refinedmods.refinedstorage2.platform.api.blockentity;

import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class NetworkNodeContainerBlockEntity<T extends NetworkNode> extends BlockEntity
        implements NetworkNodeContainer {
    private final T node;

    protected NetworkNodeContainerBlockEntity(final BlockEntityType<?> type,
                                              final BlockPos pos,
                                              final BlockState state,
                                              final T node) {
        super(type, pos, state);
        this.node = node;
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (level == null || level.isClientSide) {
            return;
        }
        PlatformApi.INSTANCE.requestNetworkNodeInitialization(this, level, this::onNetworkInNodeInitialized);
    }

    protected void onNetworkInNodeInitialized() {
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level == null || level.isClientSide) {
            return;
        }
        PlatformApi.INSTANCE.requestNetworkNodeRemoval(this, level);
    }

    @Override
    public T getNode() {
        return node;
    }

    @Override
    public String toString() {
        return "NetworkNodeContainerBlockEntity{" +
                "node=" + node +
                ", worldPosition=" + worldPosition +
                "}";
    }
}
