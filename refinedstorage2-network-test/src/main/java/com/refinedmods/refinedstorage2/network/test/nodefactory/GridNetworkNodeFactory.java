package com.refinedmods.refinedstorage2.network.test.nodefactory;

import com.refinedmods.refinedstorage2.api.network.impl.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;

import java.util.Map;

public class GridNetworkNodeFactory extends AbstractNetworkNodeFactory<GridNetworkNode> {
    @Override
    protected GridNetworkNode innerCreate(final AddNetworkNode ctx, final Map<String, Object> properties) {
        return new GridNetworkNode(getEnergyUsage(properties), NetworkTestFixtures.STORAGE_CHANNEL_TYPES);
    }
}
