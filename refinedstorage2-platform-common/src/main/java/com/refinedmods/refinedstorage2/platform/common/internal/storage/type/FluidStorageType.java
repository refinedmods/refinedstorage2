package com.refinedmods.refinedstorage2.platform.common.internal.storage.type;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.CapacityAccessor;
import com.refinedmods.refinedstorage2.api.storage.CappedStorage;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformCappedStorage;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorage;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerSource;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class FluidStorageType implements StorageType<FluidResource> {
    public static final FluidStorageType INSTANCE = new FluidStorageType();

    private static final String TAG_CAPACITY = "cap";
    private static final String TAG_STACKS = "stacks";
    private static final String TAG_CHANGED_BY = "cb";
    private static final String TAG_CHANGED_AT = "ca";

    private FluidStorageType() {
    }

    @Override
    public PlatformStorage<FluidResource> fromTag(CompoundTag tag, PlatformStorageRepository storageRepository) {
        PlatformStorage<FluidResource> storage = createStorage(tag, storageRepository);
        ListTag stacks = tag.getList(TAG_STACKS, Tag.TAG_COMPOUND);
        for (Tag stackTag : stacks) {
            FluidResource
                    .fromTagWithAmount((CompoundTag) stackTag)
                    .ifPresent(resourceAmount -> storage.load(
                            resourceAmount.getResource(),
                            resourceAmount.getAmount(),
                            ((CompoundTag) stackTag).getString(TAG_CHANGED_BY),
                            ((CompoundTag) stackTag).getLong(TAG_CHANGED_AT)
                    ));
        }
        return storage;
    }

    private PlatformStorage<FluidResource> createStorage(CompoundTag tag, PlatformStorageRepository storageRepository) {
        TrackedStorageRepository<FluidResource> trackingRepository = new InMemoryTrackedStorageRepository<>();
        if (tag.contains(TAG_CAPACITY)) {
            return new PlatformCappedStorage<>(
                    new CappedStorage<>(
                            new TrackedStorageImpl<>(new InMemoryStorageImpl<>(), trackingRepository, System::currentTimeMillis),
                            tag.getLong(TAG_CAPACITY)
                    ),
                    FluidStorageType.INSTANCE,
                    trackingRepository,
                    storageRepository::markAsChanged
            );
        }
        return new PlatformStorage<>(
                new TrackedStorageImpl<>(new InMemoryStorageImpl<>(), trackingRepository, System::currentTimeMillis),
                FluidStorageType.INSTANCE,
                trackingRepository,
                storageRepository::markAsChanged
        );
    }

    @Override
    public CompoundTag toTag(Storage<FluidResource> storage) {
        CompoundTag tag = new CompoundTag();
        if (storage instanceof CapacityAccessor capacityAccessor) {
            tag.putLong(TAG_CAPACITY, capacityAccessor.getCapacity());
        }
        ListTag stacks = new ListTag();
        for (ResourceAmount<FluidResource> resourceAmount : storage.getAll()) {
            stacks.add(toTag(storage, resourceAmount));
        }
        tag.put(TAG_STACKS, stacks);
        return tag;
    }

    private CompoundTag toTag(Storage<FluidResource> storage, ResourceAmount<FluidResource> resourceAmount) {
        CompoundTag tag = FluidResource.toTagWithAmount(resourceAmount);
        if (storage instanceof TrackedStorage<FluidResource> trackedStorage) {
            trackedStorage
                    .findTrackedResourceBySourceType(resourceAmount.getResource(), PlayerSource.class)
                    .ifPresent(trackedResource -> {
                        tag.putString(TAG_CHANGED_BY, trackedResource.getSourceName());
                        tag.putLong(TAG_CHANGED_AT, trackedResource.getTime());
                    });
        }
        return tag;
    }
}
