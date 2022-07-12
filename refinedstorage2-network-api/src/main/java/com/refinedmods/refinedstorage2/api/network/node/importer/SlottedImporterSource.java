package com.refinedmods.refinedstorage2.api.network.node.importer;

import com.refinedmods.refinedstorage2.api.core.Action;

import javax.annotation.Nullable;

public interface SlottedImporterSource<T> {
    @Nullable
    T getResource(int slot);

    int getSlots();

    long extract(int slot, long amount, Action action);
}
