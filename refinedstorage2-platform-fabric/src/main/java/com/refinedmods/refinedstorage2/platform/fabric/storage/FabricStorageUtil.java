package com.refinedmods.refinedstorage2.platform.fabric.storage;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;

final class FabricStorageUtil {
    private FabricStorageUtil() {
    }

    static <T> long getCurrentAmount(final Storage<T> storage, final T resource) {
        long amount = 0;
        final Iterable<StorageView<T>> views = storage.nonEmptyViews();
        for (final StorageView<T> view : views) {
            if (resource.equals(view.getResource())) {
                amount += view.getAmount();
            }
        }
        return amount;
    }
}
