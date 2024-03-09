package com.refinedmods.refinedstorage2.platform.api.storage;

import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.Storage;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public interface StorageType {
    Storage create(@Nullable Long capacity, Runnable listener);

    Storage fromTag(CompoundTag tag, Runnable listener);

    CompoundTag toTag(Storage storage);

    boolean isAllowed(ResourceKey resource);
}
