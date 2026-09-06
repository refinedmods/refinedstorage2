package com.refinedmods.refinedstorage.api.network.impl.node;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import java.util.List;

public class StorageContentsNetworkNodeDetails extends AbstractNetworkNodeDetails {
    private final long stored;
    private final long capacity;
    private final boolean hasCapacity;
    private final List<ResourceAmount> contents;

    public StorageContentsNetworkNodeDetails(final long energyUsage,
                                             final boolean active,
                                             final long stored,
                                             final long capacity,
                                             final boolean hasCapacity,
                                             final List<ResourceAmount> contents) {
        super(energyUsage, active);
        this.stored = stored;
        this.capacity = capacity;
        this.hasCapacity = hasCapacity;
        this.contents = List.copyOf(contents);
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

    public double getProgress() {
        if (!hasCapacity || capacity <= 0) {
            return 0;
        }
        return (double) stored / (double) capacity;
    }

    public List<ResourceAmount> getContents() {
        return contents;
    }
}
