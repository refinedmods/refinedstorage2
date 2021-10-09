package com.refinedmods.refinedstorage2.platform.fabric.api.storage.type;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageManager;

import net.minecraft.nbt.NbtCompound;

public interface StorageType<T> {
    Storage<T> fromTag(NbtCompound tag, PlatformStorageManager storageManager);

    NbtCompound toTag(Storage<T> storage);
}
