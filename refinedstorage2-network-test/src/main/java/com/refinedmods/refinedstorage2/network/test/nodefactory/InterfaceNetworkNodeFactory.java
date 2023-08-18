package com.refinedmods.refinedstorage2.network.test.nodefactory;

import com.refinedmods.refinedstorage2.api.network.impl.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;

import java.util.Map;

public class InterfaceNetworkNodeFactory extends AbstractNetworkNodeFactory<InterfaceNetworkNode> {
    @Override
    protected InterfaceNetworkNode innerCreate(final AddNetworkNode ctx, final Map<String, Object> properties) {
        return new InterfaceNetworkNode(getEnergyUsage(properties));
    }
}
