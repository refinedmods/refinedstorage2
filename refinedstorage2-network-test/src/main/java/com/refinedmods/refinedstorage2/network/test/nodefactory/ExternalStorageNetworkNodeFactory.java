package com.refinedmods.refinedstorage2.network.test.nodefactory;

import com.refinedmods.refinedstorage2.api.network.impl.node.externalstorage.ExternalStorageNetworkNode;
import com.refinedmods.refinedstorage2.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;

import java.util.Map;

public class ExternalStorageNetworkNodeFactory extends AbstractNetworkNodeFactory<ExternalStorageNetworkNode> {
    @Override
    protected ExternalStorageNetworkNode innerCreate(final AddNetworkNode ctx, final Map<String, Object> properties) {
        final ExternalStorageNetworkNode node = new ExternalStorageNetworkNode(getEnergyUsage(properties));
        node.initialize(
            NetworkTestFixtures.STORAGE_CHANNEL_TYPES,
            () -> 0L,
            type -> new InMemoryTrackedStorageRepository()
        );
        return node;
    }
}
