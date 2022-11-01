package com.refinedmods.refinedstorage2.network.test.nodefactory;

import com.refinedmods.refinedstorage2.api.network.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;

import java.util.Map;

public class InterfaceNetworkNodeFactory extends AbstractNetworkNodeFactory<InterfaceNetworkNode<String>> {
    @Override
    protected InterfaceNetworkNode<String> innerCreate(final AddNetworkNode ctx, final Map<String, Object> properties) {
        return new InterfaceNetworkNode<>(getEnergyUsage(properties), NetworkTestFixtures.STORAGE_CHANNEL_TYPE);
    }
}
