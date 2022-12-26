package com.refinedmods.refinedstorage2.platform.api.storage.channel;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelImpl;
import com.refinedmods.refinedstorage2.platform.api.resource.FuzzyModeNormalizer;
import com.refinedmods.refinedstorage2.platform.api.resource.list.FuzzyResourceList;

import java.util.Collection;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public class FuzzyStorageChannelImpl<T extends FuzzyModeNormalizer<T>> extends StorageChannelImpl<T>
    implements FuzzyStorageChannel<T> {
    private final FuzzyResourceList<T> fuzzyList;

    public FuzzyStorageChannelImpl(final FuzzyResourceList<T> fuzzyList) {
        super(fuzzyList);
        this.fuzzyList = fuzzyList;
    }

    @Override
    public Collection<ResourceAmount<T>> getFuzzy(final T resource) {
        return fuzzyList.getFuzzy(resource);
    }
}
