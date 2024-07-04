package com.refinedmods.refinedstorage.platform.common.storage;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.platform.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.platform.api.storage.StorageType;
import com.refinedmods.refinedstorage.platform.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceCodecs;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

public class ItemStorageType implements StorageType {
    ItemStorageType() {
    }

    @Override
    public SerializableStorage create(@Nullable final Long capacity, final Runnable listener) {
        return StorageTypes.createHomogeneousStorage(
            StorageTypes.ITEM,
            StorageCodecs.StorageData.empty(capacity),
            listener
        );
    }

    @Override
    public MapCodec<SerializableStorage> getMapCodec(final Runnable listener) {
        return StorageCodecs.homogeneousStorageData(
            ResourceCodecs.ITEM_CODEC
        ).xmap(storageData -> StorageTypes.createHomogeneousStorage(
            StorageTypes.ITEM,
            storageData,
            listener
        ), storage -> StorageCodecs.StorageData.ofHomogeneousStorage(
            storage,
            ItemStorageType.this::isAllowed,
            ItemResource.class::cast
        ));
    }

    @Override
    public boolean isAllowed(final ResourceKey resource) {
        return resource instanceof ItemResource;
    }

    @Override
    public long getDiskInterfaceTransferQuota(final boolean stackUpgrade) {
        return stackUpgrade ? 64 : 1;
    }

    public enum Variant {
        ONE_K("1k", 1024L),
        FOUR_K("4k", 1024 * 4L),
        SIXTEEN_K("16k", 1024 * 4 * 4L),
        SIXTY_FOUR_K("64k", 1024 * 4 * 4 * 4L),
        CREATIVE("creative", null);

        private final String name;
        @Nullable
        private final Long capacity;

        Variant(final String name, @Nullable final Long capacity) {
            this.name = name;
            this.capacity = capacity;
        }

        public String getName() {
            return name;
        }

        @Nullable
        public Long getCapacity() {
            return capacity;
        }

        public boolean hasCapacity() {
            return capacity != null;
        }
    }
}
