package com.refinedmods.refinedstorage2.network.test.nodefactory;

import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;

import java.util.Map;

@FunctionalInterface
public interface NetworkNodeFactory {
    NetworkNode create(AddNetworkNode ctx, Map<String, Object> properties);
}
