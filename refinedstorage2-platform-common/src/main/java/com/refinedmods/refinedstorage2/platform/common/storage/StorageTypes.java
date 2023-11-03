package com.refinedmods.refinedstorage2.platform.common.storage;

import com.refinedmods.refinedstorage2.platform.api.storage.StorageType;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;

public final class StorageTypes {
    public static final StorageType<ItemResource> ITEM = new ItemStorageType();
    public static final StorageType<FluidResource> FLUID = new FluidStorageType();

    private StorageTypes() {
    }
}
