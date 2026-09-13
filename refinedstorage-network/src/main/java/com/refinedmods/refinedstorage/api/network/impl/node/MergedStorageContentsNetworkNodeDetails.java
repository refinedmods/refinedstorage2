package com.refinedmods.refinedstorage.api.network.impl.node;

import com.refinedmods.refinedstorage.api.network.node.MergedNetworkNodeDetailsElement;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;

public class MergedStorageContentsNetworkNodeDetails {
    public static final MergedNetworkNodeDetailsElement<MergedStorageContentsNetworkNodeDetails> ELEMENT =
        MergedStorageContentsNetworkNodeDetails::new;

    private final MutableResourceList contents = MutableResourceListImpl.create();
    private long stored;
    private long capacity;
    private boolean hasCapacity = true;

    public void merge(final long nodeStored, final long nodeCapacity, final boolean nodeHasCapacity) {
        this.stored += nodeStored;
        this.capacity += nodeCapacity;
        this.hasCapacity &= nodeHasCapacity;
    }

    public MutableResourceList getContents() {
        return contents;
    }

    public long getStored() {
        return stored;
    }

    public long getCapacity() {
        return capacity;
    }

    public boolean hasCapacity() {
        return hasCapacity;
    }
}
