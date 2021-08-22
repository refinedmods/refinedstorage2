package com.refinedmods.refinedstorage2.platform.fabric.api.network.node.container;

import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface PlatformNetworkNodeContainer<T extends NetworkNode> extends NetworkNodeContainer<T> {
    World getContainerWorld();

    BlockPos getContainerPosition();

    void setContainerWorld(World world);
}
