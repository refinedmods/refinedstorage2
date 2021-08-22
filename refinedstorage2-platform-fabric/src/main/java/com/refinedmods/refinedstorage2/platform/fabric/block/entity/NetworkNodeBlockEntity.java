package com.refinedmods.refinedstorage2.platform.fabric.block.entity;

import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.platform.fabric.api.block.entity.NetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.api.network.node.RedstoneMode;
import com.refinedmods.refinedstorage2.platform.fabric.api.network.node.container.PlatformNetworkNodeContainer;
import com.refinedmods.refinedstorage2.platform.fabric.api.network.node.container.PlatformNetworkNodeContainerImpl;
import com.refinedmods.refinedstorage2.platform.fabric.block.NetworkNodeBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO Move to api platform module
public abstract class NetworkNodeBlockEntity<T extends NetworkNodeImpl> extends BlockEntity implements NetworkNodeContainerBlockEntity<T> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final int ACTIVE_CHANGE_MINIMUM_INTERVAL_MS = 1000;
    private static final String TAG_REDSTONE_MODE = "rm";

    private Boolean lastActive;
    private long lastActiveChanged;

    protected PlatformNetworkNodeContainerImpl<T> container;

    protected NetworkNodeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        if (!world.isClient()) {
            if (container == null) {
                T node = createNode(pos, null);
                node.setActive(false);
                container = createContainer(pos, node);
            }
            container.setContainerWorld(world);
        }
    }

    private PlatformNetworkNodeContainerImpl<T> createContainer(BlockPos pos, T node) {
        return new PlatformNetworkNodeContainerImpl<>(node, pos);
    }

    protected abstract T createNode(BlockPos pos, NbtCompound tag);

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt(TAG_REDSTONE_MODE, RedstoneModeSettings.getRedstoneMode(getRedstoneMode()));
        return super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        container = createContainer(pos, createNode(pos, nbt));

        if (nbt.contains(TAG_REDSTONE_MODE)) {
            container.setRedstoneMode(RedstoneModeSettings.getRedstoneMode(nbt.getInt(TAG_REDSTONE_MODE)));
        }
    }

    public void updateActiveness(BlockState state) {
        boolean supportsActivenessState = state.contains(NetworkNodeBlock.ACTIVE);

        if (lastActive == null) {
            lastActive = determineInitialActiveness(state, supportsActivenessState);
        }

        boolean active = container.getNode().isActive();

        if (active != lastActive && (lastActiveChanged == 0 || System.currentTimeMillis() - lastActiveChanged > ACTIVE_CHANGE_MINIMUM_INTERVAL_MS)) {
            LOGGER.info("Activeness state change for block at {}: {} -> {}", pos, lastActive, active);

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
            return state.get(NetworkNodeBlock.ACTIVE);
        }
        return container.getNode().isActive();
    }

    private void updateActivenessState(BlockState state, boolean active) {
        world.setBlockState(pos, state.with(NetworkNodeBlock.ACTIVE, active));
    }

    protected void activenessChanged(boolean active) {
    }

    public RedstoneMode getRedstoneMode() {
        return container.getRedstoneMode();
    }

    public void setRedstoneMode(RedstoneMode redstoneMode) {
        container.setRedstoneMode(redstoneMode);
        markDirty();
    }

    @Override
    public PlatformNetworkNodeContainer<T> getContainer() {
        return container;
    }
}
