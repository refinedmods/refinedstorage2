package com.refinedmods.refinedstorage.api.network.impl.node;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

public record StorageContentsChangedEvent(ResourceKey resource, long change, long stored, long capacity) {
}
