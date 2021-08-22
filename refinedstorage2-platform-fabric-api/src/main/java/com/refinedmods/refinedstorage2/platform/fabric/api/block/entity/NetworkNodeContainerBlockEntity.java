package com.refinedmods.refinedstorage2.platform.fabric.api.block.entity;

import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.platform.fabric.api.network.node.container.PlatformNetworkNodeContainer;

public interface NetworkNodeContainerBlockEntity<T extends NetworkNode> {
    PlatformNetworkNodeContainer<T> getContainer();
}
