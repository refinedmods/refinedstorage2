package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.platform.api.blockentity.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.api.network.node.PlatformNetworkNodeContainer;
import com.refinedmods.refinedstorage2.platform.common.block.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractInternalNetworkNodeContainerBlockEntity<T extends AbstractNetworkNode>
    extends AbstractNetworkNodeContainerBlockEntity<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractInternalNetworkNodeContainerBlockEntity.class);

    private static final String TAG_REDSTONE_MODE = "rm";

    private final RateLimiter activenessChangeRateLimiter = RateLimiter.create(1);
    private boolean lastActive;
    private RedstoneMode redstoneMode = RedstoneMode.IGNORE;

    protected AbstractInternalNetworkNodeContainerBlockEntity(final BlockEntityType<?> type,
                                                              final BlockPos pos,
                                                              final BlockState state,
                                                              final T node) {
        super(type, pos, state, node);
    }

    private boolean isActive() {
        final long energyUsage = getNode().getEnergyUsage();
        final boolean hasLevel = level != null && level.isLoaded(worldPosition);
        return hasLevel
            && redstoneMode.isActive(level.hasNeighborSignal(worldPosition))
            && getNode().getNetwork() != null
            && getNode().getNetwork().getComponent(EnergyNetworkComponent.class).getStored() >= energyUsage;
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_REDSTONE_MODE, RedstoneModeSettings.getRedstoneMode(getRedstoneMode()));
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_REDSTONE_MODE)) {
            redstoneMode = RedstoneModeSettings.getRedstoneMode(tag.getInt(TAG_REDSTONE_MODE));
        }
    }

    public void updateActiveness(final BlockState state,
                                 @Nullable final BooleanProperty activenessProperty) {
        final boolean newActive = isActive();
        if (newActive != lastActive && activenessChangeRateLimiter.tryAcquire()) {
            LOGGER.info("Activeness change for node at {}: {} -> {}", getBlockPos(), lastActive, newActive);
            this.lastActive = newActive;
            activenessChanged(state, newActive, activenessProperty);
        }
    }

    protected void activenessChanged(final BlockState state,
                                     final boolean newActive,
                                     @Nullable final BooleanProperty activenessProperty) {
        getNode().setActive(newActive);

        final boolean needToUpdateBlockState = activenessProperty != null
            && state.getValue(activenessProperty) != newActive;

        if (needToUpdateBlockState) {
            LOGGER.info("Sending block update for block at {} due to state change to {}", getBlockPos(), newActive);
            updateActivenessState(state, activenessProperty, newActive);
        }
    }

    private void updateActivenessState(final BlockState state,
                                       final BooleanProperty activenessProperty,
                                       final boolean active) {
        if (level != null) {
            level.setBlockAndUpdate(getBlockPos(), state.setValue(activenessProperty, active));
        }
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(final RedstoneMode redstoneMode) {
        this.redstoneMode = redstoneMode;
        setChanged();
    }

    public void doWork() {
        getNode().doWork();
    }

    @Override
    public boolean canPerformOutgoingConnection(final Direction direction) {
        final Direction myDirection = getDirection();
        if (myDirection == null) {
            return true;
        }
        return myDirection != direction;
    }

    @Override
    public boolean canAcceptIncomingConnection(final Direction direction, final PlatformNetworkNodeContainer other) {
        final Direction myDirection = getDirection();
        if (myDirection == null) {
            return true;
        }
        return myDirection != direction.getOpposite();
    }

    @Nullable
    protected final Direction getDirection() {
        final BlockState blockState = getBlockState();
        if (!(blockState.getBlock() instanceof AbstractDirectionalBlock<?> directionalBlock)) {
            return null;
        }
        return directionalBlock.extractDirection(blockState);
    }
}
