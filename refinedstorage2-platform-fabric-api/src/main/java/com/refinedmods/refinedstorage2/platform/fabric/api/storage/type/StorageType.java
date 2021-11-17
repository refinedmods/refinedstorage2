package com.refinedmods.refinedstorage2.platform.fabric.api.storage.type;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageRepository;

import net.minecraft.nbt.CompoundTag;

public interface StorageType<T> {
    Storage<T> fromTag(CompoundTag tag, PlatformStorageRepository storageManager);

    CompoundTag toTag(Storage<T> storage);
}
