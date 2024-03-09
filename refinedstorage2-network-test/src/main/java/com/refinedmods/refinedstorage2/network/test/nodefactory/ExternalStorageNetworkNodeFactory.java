package com.refinedmods.refinedstorage2.network.test.nodefactory;

import com.refinedmods.refinedstorage2.api.network.impl.node.externalstorage.ExternalStorageNetworkNode;
import com.refinedmods.refinedstorage2.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;

import java.util.Map;

public class ExternalStorageNetworkNodeFactory extends AbstractNetworkNodeFactory<ExternalStorageNetworkNode> {
    @Override
    protected ExternalStorageNetworkNode innerCreate(final AddNetworkNode ctx, final Map<String, Object> properties) {
        final ExternalStorageNetworkNode externalStorage = new ExternalStorageNetworkNode(
            getEnergyUsage(properties),
            () -> 0L
        );
        externalStorage.setTrackingRepository(new InMemoryTrackedStorageRepository());
        return externalStorage;
    }
}
