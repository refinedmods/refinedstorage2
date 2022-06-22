package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.platform.api.blockentity.NetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.NetworkNodeContainerBlock;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class InternalNetworkNodeContainerBlockEntity<T extends NetworkNodeImpl> extends NetworkNodeContainerBlockEntity<T> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final int ACTIVE_CHANGE_MINIMUM_INTERVAL_MS = 1000;
    private static final String TAG_REDSTONE_MODE = "rm";

    private Boolean lastActive;
    private long lastActiveChanged;
    private RedstoneMode redstoneMode = RedstoneMode.IGNORE;

    protected InternalNetworkNodeContainerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, T node) {
        super(type, pos, state, node);
        getNode().setActivenessProvider(() -> level.isLoaded(pos) && redstoneMode.isActive(level.hasNeighborSignal(pos)));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_REDSTONE_MODE, RedstoneModeSettings.getRedstoneMode(getRedstoneMode()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_REDSTONE_MODE)) {
            redstoneMode = RedstoneModeSettings.getRedstoneMode(tag.getInt(TAG_REDSTONE_MODE));
        }
    }

    public static void serverTick(Level level, BlockState state, InternalNetworkNodeContainerBlockEntity<?> blockEntity) {
        blockEntity.getNode().update();
        blockEntity.updateActivenessInLevel(state);
    }

    private void updateActivenessInLevel(BlockState state) {
        boolean supportsActivenessState = state.hasProperty(NetworkNodeContainerBlock.ACTIVE);

        if (lastActive == null) {
            lastActive = determineInitialActiveness(state, supportsActivenessState);
        }

        boolean active = getNode().isActive();

        if (active != lastActive && (lastActiveChanged == 0 || System.currentTimeMillis() - lastActiveChanged > ACTIVE_CHANGE_MINIMUM_INTERVAL_MS)) {
            LOGGER.info("Activeness state change for block at {}: {} -> {}", getBlockPos(), lastActive, active);

            this.lastActive = active;
            this.lastActiveChanged = System.currentTimeMillis();

            activenessChanged(active);

            if (supportsActivenessState) {
                updateActivenessState(state, active);
            }
        }
    }

    private boolean determineInitialActiveness(BlockState state, boolean supportsActivenessState) {
        if (supportsActivenessState) {
            return state.getValue(NetworkNodeContainerBlock.ACTIVE);
        }
        return getNode().isActive();
    }

    private void updateActivenessState(BlockState state, boolean active) {
        level.setBlockAndUpdate(getBlockPos(), state.setValue(NetworkNodeContainerBlock.ACTIVE, active));
    }

    protected void activenessChanged(boolean active) {
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(RedstoneMode redstoneMode) {
        this.redstoneMode = redstoneMode;
        setChanged();
    }
}
