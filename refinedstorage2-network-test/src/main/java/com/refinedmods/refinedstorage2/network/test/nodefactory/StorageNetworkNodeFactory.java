package com.refinedmods.refinedstorage2.network.test.nodefactory;

import com.refinedmods.refinedstorage2.api.network.impl.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;

import java.util.Map;

public class StorageNetworkNodeFactory extends AbstractNetworkNodeFactory<StorageNetworkNode> {
    @Override
    protected StorageNetworkNode innerCreate(final AddNetworkNode ctx, final Map<String, Object> properties) {
        return new StorageNetworkNode(getEnergyUsage(properties));
    }
}
