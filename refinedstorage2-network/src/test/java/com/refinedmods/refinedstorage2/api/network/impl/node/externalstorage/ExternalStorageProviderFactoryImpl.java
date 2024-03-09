package com.refinedmods.refinedstorage2.api.network.impl.node.externalstorage;

import com.refinedmods.refinedstorage2.api.network.node.externalstorage.ExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;

import java.util.Optional;

public record ExternalStorageProviderFactoryImpl(ExternalStorageProvider provider)
    implements ExternalStorageProviderFactory {
    @Override
    public Optional<ExternalStorageProvider> create() {
        return Optional.of(provider);
    }
}
