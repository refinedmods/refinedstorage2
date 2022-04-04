package com.refinedmods.refinedstorage2.platform.common.internal.storage.type;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.CapacityAccessor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformLimitedStorage;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorage;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerSource;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class ItemStorageType implements StorageType<ItemResource> {
    public static final ItemStorageType INSTANCE = new ItemStorageType();

    private static final String TAG_CAPACITY = "cap";
    private static final String TAG_STACKS = "stacks";
    private static final String TAG_CHANGED_BY = "cb";
    private static final String TAG_CHANGED_AT = "ca";

    private ItemStorageType() {
    }

    @Override
    public Storage<ItemResource> fromTag(CompoundTag tag, PlatformStorageRepository storageRepository) {
        PlatformStorage<ItemResource> storage = createStorage(tag, storageRepository);
        ListTag stacks = tag.getList(TAG_STACKS, Tag.TAG_COMPOUND);
        for (Tag stackTag : stacks) {
            ItemResource
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

    private PlatformStorage<ItemResource> createStorage(CompoundTag tag, PlatformStorageRepository storageRepository) {
        TrackedStorageRepository<ItemResource> trackingRepository = new InMemoryTrackedStorageRepository<>();
        if (tag.contains(TAG_CAPACITY)) {
            return new PlatformLimitedStorage<>(
                    new LimitedStorageImpl<>(
                            new TrackedStorageImpl<>(new InMemoryStorageImpl<>(), trackingRepository, System::currentTimeMillis),
                            tag.getLong(TAG_CAPACITY)
                    ),
                    ItemStorageType.INSTANCE,
                    trackingRepository,
                    storageRepository::markAsChanged
            );
        }
        return new PlatformStorage<>(
                new TrackedStorageImpl<>(new InMemoryStorageImpl<>(), trackingRepository, System::currentTimeMillis),
                ItemStorageType.INSTANCE,
                trackingRepository,
                storageRepository::markAsChanged
        );
    }

    @Override
    public CompoundTag toTag(Storage<ItemResource> storage) {
        CompoundTag tag = new CompoundTag();
        if (storage instanceof CapacityAccessor capacityAccessor) {
            tag.putLong(TAG_CAPACITY, capacityAccessor.getCapacity());
        }
        ListTag stacks = new ListTag();
        for (ResourceAmount<ItemResource> resourceAmount : storage.getAll()) {
            stacks.add(toTag(storage, resourceAmount));
        }
        tag.put(TAG_STACKS, stacks);
        return tag;
    }

    private CompoundTag toTag(Storage<ItemResource> storage, ResourceAmount<ItemResource> resourceAmount) {
        CompoundTag tag = ItemResource.toTagWithAmount(resourceAmount);
        if (storage instanceof TrackedStorage<ItemResource> trackedStorage) {
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
