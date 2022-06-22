package com.refinedmods.refinedstorage2.platform.apiimpl.storage.type;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerSource;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.LimitedPlatformStorage;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.PlatformStorage;

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
    public Storage<ItemResource> fromTag(CompoundTag tag, Runnable listener) {
        PlatformStorage<ItemResource> storage = createStorage(tag, listener);
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

    private PlatformStorage<ItemResource> createStorage(CompoundTag tag, Runnable listener) {
        TrackedStorageRepository<ItemResource> trackingRepository = new InMemoryTrackedStorageRepository<>();
        if (tag.contains(TAG_CAPACITY)) {
            return new LimitedPlatformStorage<>(
                    new LimitedStorageImpl<>(
                            new TrackedStorageImpl<>(new InMemoryStorageImpl<>(), trackingRepository, System::currentTimeMillis),
                            tag.getLong(TAG_CAPACITY)
                    ),
                    ItemStorageType.INSTANCE,
                    trackingRepository,
                    listener
            );
        }
        return new PlatformStorage<>(
                new TrackedStorageImpl<>(new InMemoryStorageImpl<>(), trackingRepository, System::currentTimeMillis),
                ItemStorageType.INSTANCE,
                trackingRepository,
                listener
        );
    }

    @Override
    public CompoundTag toTag(Storage<ItemResource> storage) {
        CompoundTag tag = new CompoundTag();
        if (storage instanceof LimitedStorage limitedStorage) {
            tag.putLong(TAG_CAPACITY, limitedStorage.getCapacity());
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

    public enum Variant {
        ONE_K("1k", 1024),
        FOUR_K("4k", 1024 * 4),
        SIXTEEN_K("16k", 1024 * 4 * 4),
        SIXTY_FOUR_K("64k", 1024 * 4 * 4 * 4),
        CREATIVE("creative", 0);

        private final String name;
        private final int capacity;

        Variant(String name, int capacity) {
            this.name = name;
            this.capacity = capacity;
        }

        public String getName() {
            return name;
        }

        public int getCapacity() {
            return capacity;
        }

        public boolean hasCapacity() {
            return capacity > 0;
        }
    }
}
