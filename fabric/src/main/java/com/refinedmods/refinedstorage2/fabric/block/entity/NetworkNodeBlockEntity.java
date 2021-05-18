package com.refinedmods.refinedstorage2.fabric.block.entity;

import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.network.node.RedstoneMode;
import com.refinedmods.refinedstorage2.core.util.Position;
import com.refinedmods.refinedstorage2.fabric.block.NetworkNodeBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class NetworkNodeBlockEntity<T extends NetworkNodeImpl> extends BlockEntity implements NetworkNode, Tickable {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final int ACTIVE_CHANGE_MINIMUM_INTERVAL_MS = 1000;
    private static final String TAG_REDSTONE_MODE = "rm";

    protected T node;
    private long lastActiveChanged;
    private CompoundTag tag;
    protected BlockState cachedState;

    protected NetworkNodeBlockEntity(BlockEntityType<?> type) {
        super(type);
    }

    @Override
    public void setLocation(World world, BlockPos pos) {
        super.setLocation(world, pos);

        if (world.isClient()) {
            return;
        }

        if (tag == null) {
            tag = new CompoundTag();
        }

        this.node = createNode(world, pos, tag);

        if (tag.contains(TAG_REDSTONE_MODE)) {
            node.setRedstoneMode(RedstoneModeSettings.getRedstoneMode(tag.getInt(TAG_REDSTONE_MODE)));
        }
    }

    protected abstract T createNode(World world, BlockPos pos, CompoundTag tag);

    @Override
    public Position getPosition() {
        return node.getPosition();
    }

    @Override
    public Network getNetwork() {
        return node.getNetwork();
    }

    @Override
    public void setNetwork(Network network) {
        node.setNetwork(network);
    }

    @Override
    public NetworkNodeReference createReference() {
        return node.createReference();
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt(TAG_REDSTONE_MODE, RedstoneModeSettings.getRedstoneMode(getRedstoneMode()));

        return super.toTag(tag);
    }

    @Override
    public void fromTag(BlockState blockState, CompoundTag tag) {
        this.tag = tag;
        super.fromTag(blockState, tag);
    }

    @Override
    public void tick() {
        if (world != null && !world.isClient() && node != null) {
            updateActivenessInWorld();
            update();
        }
    }

    private void updateActivenessInWorld() {
        calculateCachedStateIfNecessary();

        if (!cachedState.contains(NetworkNodeBlock.ACTIVE)) {
            return;
        }

        boolean active = node.isActive();
        boolean activeInWorld = cachedState.get(NetworkNodeBlock.ACTIVE);

        if (active != activeInWorld && (lastActiveChanged == 0 || System.currentTimeMillis() - lastActiveChanged > ACTIVE_CHANGE_MINIMUM_INTERVAL_MS)) {
            LOGGER.info("Activeness state change for block at {}: {} -> {}", pos, activeInWorld, active);

            this.lastActiveChanged = System.currentTimeMillis();

            onActiveChanged(active);
            updateActivenessInWorld(active);
        }
    }

    private void updateActivenessInWorld(boolean active) {
        BlockState newState = world.getBlockState(pos).with(NetworkNodeBlock.ACTIVE, active);
        updateState(newState);
    }

    @Override
    public void update() {
        node.update();
    }

    @Override
    public void onActiveChanged(boolean active) {
        node.onActiveChanged(active);
    }

    @Override
    public boolean isActive() {
        return node.isActive();
    }

    public RedstoneMode getRedstoneMode() {
        return node.getRedstoneMode();
    }

    public void setRedstoneMode(RedstoneMode redstoneMode) {
        node.setRedstoneMode(redstoneMode);
        markDirty();
    }

    protected void updateState(BlockState newState) {
        world.setBlockState(pos, newState);
        calculateCachedState();
    }

    protected void calculateCachedStateIfNecessary() {
        if (cachedState == null) {
            calculateCachedState();
        }
    }

    private void calculateCachedState() {
        this.cachedState = world.getBlockState(pos);
    }
}
