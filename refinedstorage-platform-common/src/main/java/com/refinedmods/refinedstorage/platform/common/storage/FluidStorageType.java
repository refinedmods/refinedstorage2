package com.refinedmods.refinedstorage.platform.common.storage;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.platform.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.platform.api.storage.StorageType;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceCodecs;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

public class FluidStorageType implements StorageType {
    FluidStorageType() {
    }

    @Override
    public SerializableStorage create(@Nullable final Long capacity, final Runnable listener) {
        return StorageTypes.createHomogeneousStorage(
            StorageTypes.FLUID,
            StorageCodecs.StorageData.empty(capacity),
            listener
        );
    }

    @Override
    public MapCodec<SerializableStorage> getMapCodec(final Runnable listener) {
        return StorageCodecs.homogeneousStorageData(
            ResourceCodecs.FLUID_CODEC
        ).xmap(storageData -> StorageTypes.createHomogeneousStorage(
            StorageTypes.FLUID,
            storageData,
            listener
        ), storage -> StorageCodecs.StorageData.ofHomogeneousStorage(
            storage,
            FluidStorageType.this::isAllowed,
            FluidResource.class::cast
        ));
    }

    @Override
    public boolean isAllowed(final ResourceKey resource) {
        return resource instanceof FluidResource;
    }

    @Override
    public long getDiskInterfaceTransferQuota(final boolean stackUpgrade) {
        return stackUpgrade ? Platform.INSTANCE.getBucketAmount() * 16 : Platform.INSTANCE.getBucketAmount();
    }

    public enum Variant {
        SIXTY_FOUR_B("64b", 64L),
        TWO_HUNDRED_FIFTY_SIX_B("256b", 256L),
        THOUSAND_TWENTY_FOUR_B("1024b", 1024L),
        FOUR_THOUSAND_NINETY_SIX_B("4096b", 4096L),
        CREATIVE("creative", null);

        private final String name;
        @Nullable
        private final Long capacityInBuckets;

        Variant(final String name, @Nullable final Long capacityInBuckets) {
            this.name = name;
            this.capacityInBuckets = capacityInBuckets;
        }

        public String getName() {
            return name;
        }

        @Nullable
        public Long getCapacityInBuckets() {
            return capacityInBuckets;
        }

        @Nullable
        public Long getCapacity() {
            if (capacityInBuckets == null) {
                return null;
            }
            return capacityInBuckets * Platform.INSTANCE.getBucketAmount();
        }

        public boolean hasCapacity() {
            return capacityInBuckets != null;
        }
    }
}
