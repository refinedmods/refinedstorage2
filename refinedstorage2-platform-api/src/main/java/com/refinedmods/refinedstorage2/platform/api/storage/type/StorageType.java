package com.refinedmods.refinedstorage2.platform.api.storage.type;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorageRepository;

import net.minecraft.nbt.CompoundTag;

public interface StorageType<T> {
    Storage<T> fromTag(CompoundTag tag, PlatformStorageRepository storageRepository);

    CompoundTag toTag(Storage<T> storage);
}
