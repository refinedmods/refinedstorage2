package com.refinedmods.refinedstorage2.platform.fabric.internal.storage.type;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.CappedStorage;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformCappedStorage;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.type.StorageType;

import net.fabricmc.fabric.api.util.NbtType;
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
    public Storage<ItemResource> fromTag(CompoundTag tag, PlatformStorageRepository storageManager) {
        Storage<ItemResource> storage = new PlatformCappedStorage<>(
                tag.getLong(TAG_CAPACITY),
                ItemStorageType.INSTANCE,
                storageManager::markAsChanged
        );

        ListTag stacks = tag.getList(TAG_STACKS, NbtType.COMPOUND);
        for (Tag stackTag : stacks) {
            ItemResource.fromTagWithAmount((CompoundTag) stackTag).ifPresent(resourceAmount -> storage.insert(resourceAmount.getResource(), resourceAmount.getAmount(), Action.EXECUTE));
        }
        return storage;
    }

    @Override
    public CompoundTag toTag(Storage<ItemResource> storage) {
        CompoundTag tag = new CompoundTag();
        tag.putLong(TAG_CAPACITY, ((CappedStorage) storage).getCapacity());
        ListTag stacks = new ListTag();
        for (ResourceAmount<ItemResource> resourceAmount : storage.getAll()) {
            stacks.add(ItemResource.toTagWithAmount(resourceAmount));
        }
        tag.put(TAG_STACKS, stacks);
        return tag;
    }
}
