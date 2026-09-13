package com.refinedmods.refinedstorage.api.network.impl.node;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import java.util.List;

import org.jspecify.annotations.Nullable;

public class StorageContentsNetworkNodeDetails extends AbstractNetworkNodeDetails {
    private long stored;
    private long capacity;
    private final boolean hasCapacity;
    @Nullable
    private final StorageConfigurationDetails configuration;
    private final List<ResourceAmount> contents;

    public StorageContentsNetworkNodeDetails(final long energyUsage,
                                             final boolean active,
                                             final long stored,
                                             final long capacity,
                                             final boolean hasCapacity,
                                             @Nullable final StorageConfigurationDetails configuration,
                                             final List<ResourceAmount> contents) {
        super(energyUsage, active);
        this.stored = stored;
        this.capacity = capacity;
        this.hasCapacity = hasCapacity;
        this.configuration = configuration;
        this.contents = List.copyOf(contents);
    }

    @Nullable
    public StorageConfigurationDetails getConfiguration() {
        return configuration;
    }

    public void updateStored(final long newStored, final long newCapacity) {
        this.stored = newStored;
        this.capacity = newCapacity;
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
