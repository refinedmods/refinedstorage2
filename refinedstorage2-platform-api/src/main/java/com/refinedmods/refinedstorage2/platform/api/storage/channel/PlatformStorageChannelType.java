package com.refinedmods.refinedstorage2.platform.api.storage.channel;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import java.util.function.BiConsumer;

import net.minecraft.nbt.CompoundTag;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.5")
public interface PlatformStorageChannelType<T> extends StorageChannelType<T> {
    CompoundTag toTag(T resource, TrackedResource trackedResource);

    void fromTag(CompoundTag tag, BiConsumer<T, TrackedResource> acceptor);
}
