package com.refinedmods.refinedstorage2.platform.common.internal.storage.type;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.CapacityAccessor;
import com.refinedmods.refinedstorage2.api.storage.CappedStorage;
import com.refinedmods.refinedstorage2.api.storage.EmptySource;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformCappedStorage;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorage;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class ItemStorageType implements StorageType<ItemResource> {
    public static final ItemStorageType INSTANCE = new ItemStorageType();

    private static final String TAG_CAPACITY = "cap";
    private static final String TAG_STACKS = "stacks";

    private ItemStorageType() {
    }

    @Override
    public Storage<ItemResource> fromTag(CompoundTag tag, PlatformStorageRepository storageRepository) {
        Storage<ItemResource> storage = createStorage(tag, storageRepository);
        ListTag stacks = tag.getList(TAG_STACKS, Tag.TAG_COMPOUND);
        for (Tag stackTag : stacks) {
            ItemResource.fromTagWithAmount((CompoundTag) stackTag).ifPresent(resourceAmount -> storage.insert(resourceAmount.getResource(), resourceAmount.getAmount(), Action.EXECUTE, EmptySource.INSTANCE));
        }
        return storage;
    }

    private Storage<ItemResource> createStorage(CompoundTag tag, PlatformStorageRepository storageRepository) {
        if (tag.contains(TAG_CAPACITY)) {
            return new PlatformCappedStorage<>(
                    new CappedStorage<>(tag.getLong(TAG_CAPACITY)),
                    ItemStorageType.INSTANCE,
                    storageRepository::markAsChanged
            );
        }
        return new PlatformStorage<>(
                new InMemoryStorageImpl<>(),
                ItemStorageType.INSTANCE,
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
            stacks.add(ItemResource.toTagWithAmount(resourceAmount));
        }
        tag.put(TAG_STACKS, stacks);
        return tag;
    }
}
