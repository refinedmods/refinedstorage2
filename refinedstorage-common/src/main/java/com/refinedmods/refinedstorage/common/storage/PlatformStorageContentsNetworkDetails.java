package com.refinedmods.refinedstorage.common.storage;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.impl.node.StorageContentsNetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

public class PlatformStorageContentsNetworkDetails extends AbstractNetworkNodeDetails {
    private final StorageContentsNetworkNodeDetails details;
    private final List<Optional<ResourceKey>> filters;
    private final boolean fuzzyMode;

    public PlatformStorageContentsNetworkDetails(final StorageContentsNetworkNodeDetails details,
                                                 final List<Optional<ResourceKey>> filters,
                                                 final boolean fuzzyMode) {
        super(details.getEnergyUsage(), details.isActive());
        this.details = details;
        this.filters = List.copyOf(filters);
        this.fuzzyMode = fuzzyMode;
    }

    @Nullable
    public static StorageContentsNetworkNodeDetails unwrap(final NetworkNodeDetails details) {
        if (details instanceof PlatformStorageContentsNetworkDetails platformDetails) {
            return platformDetails.details;
        }
        if (details instanceof StorageContentsNetworkNodeDetails storageDetails) {
            return storageDetails;
        }
        return null;
    }

    public StorageContentsNetworkNodeDetails getDetails() {
        return details;
    }

    public List<Optional<ResourceKey>> getFilters() {
        return filters;
    }

    public boolean isFuzzyMode() {
        return fuzzyMode;
    }
}
