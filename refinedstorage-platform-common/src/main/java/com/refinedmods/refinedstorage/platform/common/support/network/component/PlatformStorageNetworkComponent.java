package com.refinedmods.refinedstorage.platform.common.support.network.component;

import com.refinedmods.refinedstorage.api.network.impl.storage.StorageNetworkComponentImpl;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage.platform.api.storage.channel.FuzzyStorageChannel;
import com.refinedmods.refinedstorage.platform.api.support.resource.list.FuzzyResourceList;
import com.refinedmods.refinedstorage.platform.common.support.resource.list.FuzzyResourceListImpl;

import java.util.Collection;

public class PlatformStorageNetworkComponent extends StorageNetworkComponentImpl implements FuzzyStorageChannel {
    private final FuzzyResourceList fuzzyResourceList;

    public PlatformStorageNetworkComponent() {
        this(new FuzzyResourceListImpl(new ResourceListImpl()));
    }

    private PlatformStorageNetworkComponent(final FuzzyResourceList fuzzyResourceList) {
        super(fuzzyResourceList);
        this.fuzzyResourceList = fuzzyResourceList;
    }

    @Override
    public Collection<ResourceAmount> getFuzzy(final ResourceKey resource) {
        return fuzzyResourceList.getFuzzy(resource);
    }
}
