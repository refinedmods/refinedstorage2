package com.refinedmods.refinedstorage.common.api.storage;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.List;
import java.util.Optional;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "3.0.0")
public record StorageContents(StorageType type, Optional<Long> capacity, List<Stored> stored) {
    public record Stored(ResourceKey resource, long amount, Optional<Changed> changed) {
    }

    public record Changed(String by, long at) {
    }
}
