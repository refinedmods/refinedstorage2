package com.refinedmods.refinedstorage2.platform.fabric.api.storage.type;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageRepository;

import net.minecraft.nbt.NbtCompound;

public interface StorageType<T> {
    Storage<T> fromTag(NbtCompound tag, PlatformStorageRepository storageManager);

    NbtCompound toTag(Storage<T> storage);
}
