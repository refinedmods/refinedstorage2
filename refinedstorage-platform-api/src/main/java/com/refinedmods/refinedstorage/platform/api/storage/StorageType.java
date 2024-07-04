package com.refinedmods.refinedstorage.platform.api.storage;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public interface StorageType {
    SerializableStorage create(@Nullable Long capacity, Runnable listener);

    MapCodec<SerializableStorage> getMapCodec(Runnable listener);

    boolean isAllowed(ResourceKey resource);

    long getDiskInterfaceTransferQuota(boolean stackUpgrade);
}
