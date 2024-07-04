package com.refinedmods.refinedstorage.platform.api.storage.channel;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.channel.StorageChannel;

import java.util.Collection;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public interface FuzzyStorageChannel extends StorageChannel {
    Collection<ResourceAmount> getFuzzy(ResourceKey resource);
}
