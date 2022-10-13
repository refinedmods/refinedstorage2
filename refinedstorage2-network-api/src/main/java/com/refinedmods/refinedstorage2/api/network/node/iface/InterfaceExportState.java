package com.refinedmods.refinedstorage2.api.network.node.iface;

import javax.annotation.Nullable;

public interface InterfaceExportState<T> {
    int getSlots();

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
