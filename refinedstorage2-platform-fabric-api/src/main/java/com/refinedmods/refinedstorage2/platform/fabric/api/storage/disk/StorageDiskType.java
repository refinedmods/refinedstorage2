package com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk;

import com.refinedmods.refinedstorage2.api.storage.disk.StorageDisk;

import net.minecraft.nbt.NbtCompound;

public interface StorageDiskType<T> {
    StorageDisk<T> fromTag(NbtCompound tag, PlatformStorageDiskManager platformStorageDiskManager);

    NbtCompound toTag(StorageDisk<T> disk);
}
