package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.platform.api.blockentity.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO: this should just block everything outgoing if directional, should also work when wrenching.
public abstract class AbstractInternalNetworkNodeContainerBlockEntity<T extends AbstractNetworkNode>
    extends AbstractNetworkNodeContainerBlockEntity<T> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final int ACTIVE_CHANGE_MINIMUM_INTERVAL_MS = 1000;
    private static final String TAG_REDSTONE_MODE = "rm";

    @Nullable
    private Boolean lastActive;
    private long lastActiveChanged;
    private RedstoneMode redstoneMode = RedstoneMode.IGNORE;

    protected AbstractInternalNetworkNodeContainerBlockEntity(final BlockEntityType<?> type,
                                                              final BlockPos pos,
                                                              final BlockState state,
                                                              final T node) {
        super(type, pos, state, node);
        getNode().setActivenessProvider(this::isActive);
    }

    private boolean isActive() {
        return level != null
            && level.isLoaded(worldPosition)
            && redstoneMode.isActive(level.hasNeighborSignal(worldPosition));
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

    public void updateActivenessInLevel(final BlockState state,
                                        @Nullable final BooleanProperty activenessProperty) {
        if (lastActive == null) {
            lastActive = determineInitialActiveness(state, activenessProperty);
        }

        final boolean active = getNode().isActive();
        final boolean inTime = System.currentTimeMillis() - lastActiveChanged > ACTIVE_CHANGE_MINIMUM_INTERVAL_MS;

        if (active != lastActive && (lastActiveChanged == 0 || inTime)) {
            LOGGER.info("Activeness state change for block at {}: {} -> {}", getBlockPos(), lastActive, active);

            this.lastActive = active;
            this.lastActiveChanged = System.currentTimeMillis();

            activenessChanged(active);

            if (activenessProperty != null) {
                updateActivenessState(state, activenessProperty, active);
            }
        }
    }

    private boolean determineInitialActiveness(final BlockState state,
                                               @Nullable final BooleanProperty activenessProperty) {
        if (activenessProperty != null) {
            return state.getValue(activenessProperty);
        }
        return getNode().isActive();
    }

    private void updateActivenessState(final BlockState state,
                                       final BooleanProperty activenessProperty,
                                       final boolean active) {
        if (level != null) {
            level.setBlockAndUpdate(getBlockPos(), state.setValue(activenessProperty, active));
        }
    }

    protected void activenessChanged(final boolean active) {
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(final RedstoneMode redstoneMode) {
        this.redstoneMode = redstoneMode;
        setChanged();
    }
}
