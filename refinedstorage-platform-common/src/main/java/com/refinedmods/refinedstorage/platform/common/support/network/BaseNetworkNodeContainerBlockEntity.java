package com.refinedmods.refinedstorage.platform.common.support.network;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.platform.api.support.network.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.platform.api.support.network.ConnectionLogic;
import com.refinedmods.refinedstorage.platform.api.support.network.ConnectionSink;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.NetworkBoundItemTargetBlockEntity;
import com.refinedmods.refinedstorage.platform.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.platform.common.support.ColorableBlock;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseNetworkNodeContainerBlockEntity<T extends AbstractNetworkNode>
    extends AbstractNetworkNodeContainerBlockEntity<T>
    implements ConnectionLogic, NetworkBoundItemTargetBlockEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseNetworkNodeContainerBlockEntity.class);

    private final RateLimiter activenessChangeRateLimiter = RateLimiter.create(1);

    public BaseNetworkNodeContainerBlockEntity(final BlockEntityType<?> type,
                                               final BlockPos pos,
                                               final BlockState state,
                                               final T networkNode) {
        super(type, pos, state, networkNode);
    }

    protected boolean calculateActive() {
        final long energyUsage = mainNode.getEnergyUsage();
        final boolean hasLevel = level != null && level.isLoaded(worldPosition);
        return hasLevel
            && mainNode.getNetwork() != null
            && mainNode.getNetwork().getComponent(EnergyNetworkComponent.class).getStored() >= energyUsage;
    }

    public void updateActiveness(final BlockState state, @Nullable final BooleanProperty activenessProperty) {
        final boolean newActive = calculateActive();
        final boolean nodeActivenessNeedsUpdate = newActive != mainNode.isActive();
        final boolean blockStateActivenessNeedsUpdate = activenessProperty != null
            && state.getValue(activenessProperty) != newActive;
        final boolean activenessNeedsUpdate = nodeActivenessNeedsUpdate || blockStateActivenessNeedsUpdate;
        if (activenessNeedsUpdate && activenessChangeRateLimiter.tryAcquire()) {
            if (nodeActivenessNeedsUpdate) {
                activenessChanged(newActive);
            }
            if (blockStateActivenessNeedsUpdate) {
                updateActivenessBlockState(state, activenessProperty, newActive);
            }
        }
    }

    protected void activenessChanged(final boolean newActive) {
        LOGGER.debug("Activeness change for node at {}: {} -> {}", getBlockPos(), mainNode.isActive(), newActive);
        mainNode.setActive(newActive);
    }

    private void updateActivenessBlockState(final BlockState state,
                                            final BooleanProperty activenessProperty,
                                            final boolean active) {
        if (level != null) {
            LOGGER.debug(
                "Sending block update at {} due to activeness change: {} -> {}",
                getBlockPos(),
                state.getValue(activenessProperty),
                active
            );
            level.setBlockAndUpdate(getBlockPos(), state.setValue(activenessProperty, active));
        }
    }

    public void doWork() {
        mainNode.doWork();
    }

    @Override
    public void addOutgoingConnections(final ConnectionSink sink) {
        final Direction myDirection = getDirection();
        if (myDirection == null) {
            super.addOutgoingConnections(sink);
            return;
        }
        for (final Direction direction : Direction.values()) {
            if (direction == myDirection) {
                continue;
            }
            sink.tryConnectInSameDimension(worldPosition.relative(direction), direction.getOpposite());
        }
    }

    @Override
    public boolean canAcceptIncomingConnection(final Direction incomingDirection, final BlockState connectingState) {
        if (!colorsAllowConnecting(connectingState)) {
            return false;
        }
        final Direction myDirection = getDirection();
        if (myDirection != null) {
            return myDirection != incomingDirection;
        }
        return true;
    }

    protected final boolean colorsAllowConnecting(final BlockState connectingState) {
        if (!(connectingState.getBlock() instanceof ColorableBlock<?, ?> otherColorableBlock)) {
            return true;
        }
        final ColorableBlock<?, ?> colorableBlock = getColor();
        if (colorableBlock == null) {
            return true;
        }
        return otherColorableBlock.getColor() == colorableBlock.getColor()
            || colorableBlock.canAlwaysConnect()
            || otherColorableBlock.canAlwaysConnect();
    }

    @Nullable
    private ColorableBlock<?, ?> getColor() {
        if (!(getBlockState().getBlock() instanceof ColorableBlock<?, ?> colorableBlock)) {
            return null;
        }
        return colorableBlock;
    }

    @Nullable
    protected final Direction getDirection() {
        final BlockState blockState = getBlockState();
        if (!(blockState.getBlock() instanceof AbstractDirectionalBlock<?> directionalBlock)) {
            return null;
        }
        return directionalBlock.extractDirection(blockState);
    }

    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(
        final BlockState oldBlockState,
        final BlockState newBlockState
    ) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setBlockState(final BlockState newBlockState) {
        final BlockState oldBlockState = getBlockState();
        super.setBlockState(newBlockState);
        if (!doesBlockStateChangeWarrantNetworkNodeUpdate(oldBlockState, newBlockState)) {
            return;
        }
        updateContainers();
    }

    @Nullable
    @Override
    public Network getNetworkForBoundItem() {
        return mainNode.getNetwork();
    }
}
