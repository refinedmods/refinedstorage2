package com.refinedmods.refinedstorage2.platform.fabric.api.storage.bulk;

import com.refinedmods.refinedstorage2.api.storage.bulk.BulkStorage;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageManager;

import net.minecraft.nbt.NbtCompound;

public interface StorageDiskType<T> {
    BulkStorage<T> fromTag(NbtCompound tag, PlatformStorageManager platformStorageDiskManager);

    NbtCompound toTag(BulkStorage<T> disk);
}
