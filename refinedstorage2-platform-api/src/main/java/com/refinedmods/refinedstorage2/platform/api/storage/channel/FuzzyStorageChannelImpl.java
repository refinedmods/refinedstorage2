package com.refinedmods.refinedstorage2.platform.api.storage.channel;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelImpl;
import com.refinedmods.refinedstorage2.platform.api.support.resource.list.FuzzyResourceList;

import java.util.Collection;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public class FuzzyStorageChannelImpl extends StorageChannelImpl implements FuzzyStorageChannel {
    private final FuzzyResourceList fuzzyList;

    public FuzzyStorageChannelImpl(final FuzzyResourceList fuzzyList) {
        super(fuzzyList);
        this.fuzzyList = fuzzyList;
    }

    @Override
    public Collection<ResourceAmount> getFuzzy(final ResourceKey resource) {
        return fuzzyList.getFuzzy(resource);
    }
}
