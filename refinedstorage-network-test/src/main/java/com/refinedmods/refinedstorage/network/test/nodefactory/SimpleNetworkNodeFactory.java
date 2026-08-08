package com.refinedmods.refinedstorage.network.test.nodefactory;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeType;

import java.util.Map;

public class SimpleNetworkNodeFactory extends AbstractNetworkNodeFactory {
    public static final NetworkNodeType TYPE = new NetworkNodeType() {
    };

    @Override
    protected AbstractNetworkNode innerCreate(final Map<String, Object> properties) {
        return new SimpleNetworkNode(TYPE, getEnergyUsage(properties));
    }
}
