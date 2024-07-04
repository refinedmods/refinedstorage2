package com.refinedmods.refinedstorage.network.test.nodefactory;

import com.refinedmods.refinedstorage.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;

import java.util.Map;

@FunctionalInterface
public interface NetworkNodeFactory {
    NetworkNode create(AddNetworkNode ctx, Map<String, Object> properties);
}
