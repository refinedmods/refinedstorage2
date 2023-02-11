package com.refinedmods.refinedstorage2.platform.api.blockentity;

import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.network.node.PlatformNetworkNodeContainer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public abstract class AbstractNetworkNodeContainerBlockEntity<T extends NetworkNode> extends BlockEntity
    implements PlatformNetworkNodeContainer {
    private final T node;

    protected AbstractNetworkNodeContainerBlockEntity(final BlockEntityType<?> type,
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
    @SuppressWarnings("deprecation")
    public void setBlockState(final BlockState newBlockState) {
        super.setBlockState(newBlockState);
        if (level == null || level.isClientSide || node.getNetwork() == null) {
            return;
        }
        PlatformApi.INSTANCE.requestNetworkNodeUpdate(this, level);
    }

    @Override
    public T getNode() {
        return node;
    }

    @Override
    public boolean canPerformOutgoingConnection(final Direction direction) {
        return true;
    }

    @Override
    public boolean canAcceptIncomingConnection(final Direction direction, final BlockState other) {
        return true;
    }

    @Override
    public String toString() {
        return "NetworkNodeContainerBlockEntity{"
            + "node=" + node
            + ", worldPosition=" + worldPosition
            + "}";
    }
}
