package com.refinedmods.refinedstorage.platform.api.storage;

import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.platform.api.PlatformApi;

import com.mojang.serialization.Codec;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public interface SerializableStorage extends Storage {
    StorageType getType();

    static Codec<SerializableStorage> getCodec(final Runnable listener) {
        return PlatformApi.INSTANCE.getStorageTypeRegistry()
            .codec()
            .dispatch(SerializableStorage::getType, storage -> storage.getMapCodec(listener));
    }
}
