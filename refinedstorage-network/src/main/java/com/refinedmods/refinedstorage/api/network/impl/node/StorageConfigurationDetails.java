package com.refinedmods.refinedstorage.api.network.impl.node;

import com.refinedmods.refinedstorage.api.network.impl.storage.StorageConfiguration;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.AccessMode;

public record StorageConfigurationDetails(FilterMode filterMode,
                                          AccessMode accessMode,
                                          int insertPriority,
                                          int extractPriority,
                                          boolean voidExcess) {
    public static StorageConfigurationDetails of(final StorageConfiguration configuration) {
        return new StorageConfigurationDetails(
            configuration.getFilterMode(),
            configuration.getAccessMode(),
            configuration.getInsertPriority(),
            configuration.getExtractPriority(),
            configuration.isVoidExcess()
        );
    }
}
