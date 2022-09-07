package com.refinedmods.refinedstorage2.platform.api.storage.channel;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;

import java.util.Collection;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public interface FuzzyStorageChannel<T> extends StorageChannel<T> {
    Collection<ResourceAmount<T>> getFuzzy(T resource);
}
