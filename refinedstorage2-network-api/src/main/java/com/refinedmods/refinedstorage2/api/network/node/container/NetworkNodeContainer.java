package com.refinedmods.refinedstorage2.api.network.node.container;

import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
@FunctionalInterface
public interface NetworkNodeContainer {
    NetworkNode getNode();
}
