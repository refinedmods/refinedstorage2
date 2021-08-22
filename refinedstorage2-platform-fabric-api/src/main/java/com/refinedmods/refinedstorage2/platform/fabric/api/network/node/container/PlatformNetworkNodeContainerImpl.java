package com.refinedmods.refinedstorage2.platform.fabric.api.network.node.container;

import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainerImpl;
import com.refinedmods.refinedstorage2.platform.fabric.api.network.node.RedstoneMode;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlatformNetworkNodeContainerImpl<T extends NetworkNodeImpl> extends NetworkNodeContainerImpl<T> implements PlatformNetworkNodeContainer<T> {
    private World world;
    private final BlockPos pos;
    private RedstoneMode redstoneMode = RedstoneMode.IGNORE;

    public PlatformNetworkNodeContainerImpl(T node, BlockPos pos) {
        super(node);
        this.pos = pos;
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(RedstoneMode redstoneMode) {
        this.redstoneMode = redstoneMode;
    }

    @Override
    public World getContainerWorld() {
        return world;
    }

    @Override
    public BlockPos getContainerPosition() {
        return pos;
    }

    @Override
    public void setContainerWorld(World world) {
        this.world = world;
    }

    @Override
    protected boolean isActive() {
        return super.isActive() && redstoneMode.isActive(world.isReceivingRedstonePower(pos));
    }

    @Override
    public void update() {
        super.update();
    }
}
