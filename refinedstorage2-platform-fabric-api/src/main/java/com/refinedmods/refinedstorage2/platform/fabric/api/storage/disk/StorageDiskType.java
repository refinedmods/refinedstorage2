package com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk;

import com.refinedmods.refinedstorage2.api.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageManager;

import net.minecraft.nbt.NbtCompound;

public interface StorageDiskType<T> {
    StorageDisk<T> fromTag(NbtCompound tag, PlatformStorageManager platformStorageDiskManager);

    NbtCompound toTag(StorageDisk<T> disk);
}
