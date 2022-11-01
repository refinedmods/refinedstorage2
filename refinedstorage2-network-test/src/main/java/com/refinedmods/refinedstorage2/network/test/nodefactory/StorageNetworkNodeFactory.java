package com.refinedmods.refinedstorage2.network.test.nodefactory;

import com.refinedmods.refinedstorage2.api.network.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;

import java.util.Map;

public class StorageNetworkNodeFactory extends AbstractNetworkNodeFactory<StorageNetworkNode<String>> {
    @Override
    protected StorageNetworkNode<String> innerCreate(final AddNetworkNode ctx, final Map<String, Object> properties) {
        return new StorageNetworkNode<>(
            getEnergyUsage(properties),
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE
        );
    }
}
