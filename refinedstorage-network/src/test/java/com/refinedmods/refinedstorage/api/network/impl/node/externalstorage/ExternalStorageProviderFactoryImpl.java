package com.refinedmods.refinedstorage.api.network.impl.node.externalstorage;

import com.refinedmods.refinedstorage.api.network.node.externalstorage.ExternalStorageProviderFactory;
import com.refinedmods.refinedstorage.api.storage.external.ExternalStorageProvider;

import java.util.Optional;

public record ExternalStorageProviderFactoryImpl(ExternalStorageProvider provider)
    implements ExternalStorageProviderFactory {
    @Override
    public Optional<ExternalStorageProvider> create() {
        return Optional.of(provider);
    }
}
