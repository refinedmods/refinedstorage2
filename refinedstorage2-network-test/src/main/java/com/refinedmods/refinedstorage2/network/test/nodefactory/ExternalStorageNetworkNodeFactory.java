package com.refinedmods.refinedstorage2.network.test.nodefactory;

import com.refinedmods.refinedstorage2.api.network.node.externalstorage.ExternalStorageNetworkNode;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;

import java.util.Map;

public class ExternalStorageNetworkNodeFactory extends AbstractNetworkNodeFactory<ExternalStorageNetworkNode> {
    @Override
    protected ExternalStorageNetworkNode innerCreate(final AddNetworkNode ctx, final Map<String, Object> properties) {
        return new ExternalStorageNetworkNode(
            getEnergyUsage(properties),
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE_REGISTRY
        );
    }
}
