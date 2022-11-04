package com.refinedmods.refinedstorage2.api.network.node.externalstorage;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;

import java.util.Optional;

public record ExternalStorageProviderFactoryImpl(ExternalStorageProvider<String> provider)
    implements ExternalStorageProviderFactory {
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<ExternalStorageProvider<T>> create(final StorageChannelType<T> channelType) {
        if (channelType == NetworkTestFixtures.STORAGE_CHANNEL_TYPE) {
            return Optional.of((ExternalStorageProvider<T>) provider);
        }
        return Optional.empty();
    }
}
