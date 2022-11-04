package com.refinedmods.refinedstorage2.api.network.node.iface;

import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;

import java.util.Collection;
import javax.annotation.Nullable;

public interface InterfaceExportState<T> extends InsertableStorage<T> {
    int getSlots();

    Collection<T> expandExportCandidates(StorageChannel<T> storageChannel, T resource);

    boolean isCurrentlyExportedResourceValid(T want, T got);

    @Nullable
    T getRequestedResource(int index);

    long getRequestedResourceAmount(int index);

    @Nullable
    T getCurrentlyExportedResource(int index);

    long getCurrentlyExportedResourceAmount(int index);

    void setCurrentlyExported(int index, T resource, long amount);

    void decrementCurrentlyExportedAmount(int index, long amount);

    void incrementCurrentlyExportedAmount(int index, long amount);
}
