package com.refinedmods.refinedstorage2.fabric.block.entity;

import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.network.node.RedstoneMode;
import com.refinedmods.refinedstorage2.fabric.block.GridBlock;
import com.refinedmods.refinedstorage2.fabric.block.NetworkNodeBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class NetworkNodeBlockEntity<T extends NetworkNodeImpl> extends BlockEntity implements NetworkNode, Tickable {
    protected T node;
    private boolean lastActive;
    private long lastActiveChanged;

    public NetworkNodeBlockEntity(BlockEntityType<?> type) {
        super(type);
    }

    @Override
    public void setLocation(World world, BlockPos pos) {
        super.setLocation(world, pos);

        node = createNode(world, pos);
    }

    protected abstract T createNode(World world, BlockPos pos);

    @Override
    public BlockPos getPosition() {
        return node.getPosition();
    }

    @Override
    public void setNetwork(Network network) {
        node.setNetwork(network);
    }

    @Override
    public Network getNetwork() {
        return node.getNetwork();
    }

    @Override
    public NetworkNodeReference createReference() {
        return node.createReference();
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt("rm", RedstoneModeSettings.getRedstoneMode(getRedstoneMode()));

        return super.toTag(tag);
    }

    @Override
    public void fromTag(BlockState blockState, CompoundTag tag) {
        if (tag.contains("rm")) {
            node.setRedstoneMode(RedstoneModeSettings.getRedstoneMode(tag.getInt("rm")));
        }

        super.fromTag(blockState, tag);
    }

    @Override
    public void tick() {
        if (world != null && !world.isClient() && node != null) {
            boolean active = node.isActive();
            if (active != lastActive && (lastActiveChanged == 0 || System.currentTimeMillis() - lastActiveChanged > 1000)) {
                this.lastActiveChanged = System.currentTimeMillis();
                this.lastActive = active;

                onActiveChanged(active);
                updateState(active);
            }
        }
    }

    private void updateState(boolean active) {
        BlockState state = world.getBlockState(pos);
        if (state.contains(NetworkNodeBlock.ACTIVE)) {
            world.setBlockState(pos, world.getBlockState(pos).with(GridBlock.ACTIVE, active));
        }
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
}
