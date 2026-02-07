package com.refinedmods.refinedstorage.common.api.storage;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import com.mojang.serialization.MapCodec;
import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public interface StorageType {
    SerializableStorage create(@Nullable Long capacity, Runnable listener);

    SerializableStorage create(StorageContents contents, Runnable listener);

    MapCodec<StorageContents> getCodec();

    boolean isAllowed(ResourceKey resource);

    long getDiskInterfaceTransferQuota(boolean stackUpgrade);
}
