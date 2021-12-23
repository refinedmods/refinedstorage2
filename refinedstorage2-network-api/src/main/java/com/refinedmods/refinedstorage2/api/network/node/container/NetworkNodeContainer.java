package com.refinedmods.refinedstorage2.api.network.node.container;

import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;

@FunctionalInterface
public interface NetworkNodeContainer {
    NetworkNode getNode();
}
