package com.refinedmods.refinedstorage2.platform.common.storage;

import com.refinedmods.refinedstorage2.platform.api.storage.StorageType;

public final class StorageTypes {
    public static final StorageType ITEM = new ItemStorageType();
    public static final StorageType FLUID = new FluidStorageType();

    private StorageTypes() {
    }
}
