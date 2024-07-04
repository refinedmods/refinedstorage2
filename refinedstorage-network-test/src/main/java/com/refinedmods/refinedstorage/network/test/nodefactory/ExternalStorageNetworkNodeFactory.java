package com.refinedmods.refinedstorage.network.test.nodefactory;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.externalstorage.ExternalStorageNetworkNode;
import com.refinedmods.refinedstorage.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;

import java.util.Map;

public class ExternalStorageNetworkNodeFactory extends AbstractNetworkNodeFactory {
    @Override
    protected AbstractNetworkNode innerCreate(final AddNetworkNode ctx, final Map<String, Object> properties) {
        final ExternalStorageNetworkNode externalStorage = new ExternalStorageNetworkNode(
            getEnergyUsage(properties),
            () -> 0L
        );
        externalStorage.setTrackingRepository(new InMemoryTrackedStorageRepository());
        return externalStorage;
    }
}
