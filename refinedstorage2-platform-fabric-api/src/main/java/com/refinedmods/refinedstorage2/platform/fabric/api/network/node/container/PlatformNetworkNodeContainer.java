package com.refinedmods.refinedstorage2.platform.fabric.api.network.node.container;

import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface PlatformNetworkNodeContainer<T extends NetworkNode> extends NetworkNodeContainer<T> {
    Level getContainerLevel();

    void setContainerLevel(Level level);

    BlockPos getContainerPosition();

    void initialize();
}
