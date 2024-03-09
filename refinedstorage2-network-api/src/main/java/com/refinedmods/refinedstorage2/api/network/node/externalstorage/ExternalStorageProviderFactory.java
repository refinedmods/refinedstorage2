package com.refinedmods.refinedstorage2.api.network.node.externalstorage;

import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;

import java.util.Optional;

import org.apiguardian.api.API;

/**
 * Provides the {@link com.refinedmods.refinedstorage2.api.storage.external.ExternalStorage}
 * with an {@link ExternalStorageProvider}.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
@FunctionalInterface
public interface ExternalStorageProviderFactory {
    /**
     * @return the external storage provider, if present
     */
    Optional<ExternalStorageProvider> create();
}
