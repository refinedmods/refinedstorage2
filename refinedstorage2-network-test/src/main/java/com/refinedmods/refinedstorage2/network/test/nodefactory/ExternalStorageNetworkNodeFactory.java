package com.refinedmods.refinedstorage2.network.test.nodefactory;

import com.refinedmods.refinedstorage2.api.network.impl.node.externalstorage.ExternalStorageNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.externalstorage.TrackedStorageRepositoryProvider;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageRepository;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;

import java.util.Map;

public class ExternalStorageNetworkNodeFactory extends AbstractNetworkNodeFactory<ExternalStorageNetworkNode> {
    @Override
    protected ExternalStorageNetworkNode innerCreate(final AddNetworkNode ctx, final Map<String, Object> properties) {
        final ExternalStorageNetworkNode node = new ExternalStorageNetworkNode(getEnergyUsage(properties));
        node.initialize(
            NetworkTestFixtures.STORAGE_CHANNEL_TYPE_REGISTRY,
            () -> 0L,
            new TrackedStorageRepositoryProvider() {
                @Override
                public <T> TrackedStorageRepository<T> getRepository(final StorageChannelType<T> type) {
                    return new InMemoryTrackedStorageRepository<>();
                }
            }
        );
        return node;
    }
}
