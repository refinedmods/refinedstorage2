package com.refinedmods.refinedstorage2.network.test.nodefactory;

import com.refinedmods.refinedstorage2.api.network.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;

import java.util.Map;

public class SimpleNetworkNodeFactory extends AbstractNetworkNodeFactory<SimpleNetworkNode> {
    @Override
    protected SimpleNetworkNode innerCreate(final AddNetworkNode ctx, final Map<String, Object> properties) {
        return new SimpleNetworkNode(getEnergyUsage(properties));
    }
}
