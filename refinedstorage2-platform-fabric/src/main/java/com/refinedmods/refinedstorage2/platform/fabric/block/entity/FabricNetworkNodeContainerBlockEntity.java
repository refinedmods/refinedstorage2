package com.refinedmods.refinedstorage2.platform.fabric.block.entity;

import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.platform.fabric.api.block.entity.NetworkNodeContainerBlockEntityImpl;
import com.refinedmods.refinedstorage2.platform.fabric.api.network.node.RedstoneMode;
import com.refinedmods.refinedstorage2.platform.fabric.api.network.node.container.PlatformNetworkNodeContainerImpl;
import com.refinedmods.refinedstorage2.platform.fabric.block.NetworkNodeContainerBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class FabricNetworkNodeContainerBlockEntity<T extends NetworkNodeImpl> extends NetworkNodeContainerBlockEntityImpl<T, PlatformNetworkNodeContainerImpl<T>> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final int ACTIVE_CHANGE_MINIMUM_INTERVAL_MS = 1000;
    private static final String TAG_REDSTONE_MODE = "rm";

    private Boolean lastActive;
    private long lastActiveChanged;

    protected FabricNetworkNodeContainerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.putInt(TAG_REDSTONE_MODE, RedstoneModeSettings.getRedstoneMode(getRedstoneMode()));
        return super.save(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        if (nbt.contains(TAG_REDSTONE_MODE)) {
            getContainer().setRedstoneMode(RedstoneModeSettings.getRedstoneMode(nbt.getInt(TAG_REDSTONE_MODE)));
        }
    }

    public void updateActiveness(BlockState state) {
        boolean supportsActivenessState = state.hasProperty(NetworkNodeContainerBlock.ACTIVE);

        if (lastActive == null) {
            lastActive = determineInitialActiveness(state, supportsActivenessState);
        }

        boolean active = getContainer().getNode().isActive();

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
        return getContainer().getNode().isActive();
    }

    private void updateActivenessState(BlockState state, boolean active) {
        level.setBlockAndUpdate(getBlockPos(), state.setValue(NetworkNodeContainerBlock.ACTIVE, active));
    }

    protected void activenessChanged(boolean active) {
    }

    public RedstoneMode getRedstoneMode() {
        return getContainer().getRedstoneMode();
    }

    public void setRedstoneMode(RedstoneMode redstoneMode) {
        getContainer().setRedstoneMode(redstoneMode);
        setChanged();
    }
}
