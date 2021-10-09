package com.refinedmods.refinedstorage2.platform.fabric.internal.storage.type;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.bulk.BulkStorage;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageManager;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.bulk.BulkPlatformStorage;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.type.StorageType;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class FluidBulkStorageType implements StorageType<FluidResource> {
    public static final FluidBulkStorageType INSTANCE = new FluidBulkStorageType();

    private static final String TAG_CAPACITY = "cap";
    private static final String TAG_STACKS = "stacks";

    private FluidBulkStorageType() {
    }

    @Override
    public BulkStorage<FluidResource> fromTag(NbtCompound tag, PlatformStorageManager storageManager) {
        BulkStorage<FluidResource> storage = new BulkPlatformStorage<>(
                tag.getLong(TAG_CAPACITY),
                FluidBulkStorageType.INSTANCE,
                storageManager::markAsChanged
        );

        NbtList stacks = tag.getList(TAG_STACKS, NbtType.COMPOUND);
        for (NbtElement stackTag : stacks) {
            FluidResource.fromTagWithAmount((NbtCompound) stackTag).ifPresent(resourceAmount -> storage.insert(resourceAmount.getResource(), resourceAmount.getAmount(), Action.EXECUTE));
        }
        return storage;
    }

    @Override
    public NbtCompound toTag(Storage<FluidResource> storage) {
        NbtCompound tag = new NbtCompound();
        tag.putLong(TAG_CAPACITY, ((BulkStorage) storage).getCapacity());
        NbtList stacks = new NbtList();
        for (ResourceAmount<FluidResource> resourceAmount : storage.getAll()) {
            stacks.add(FluidResource.toTagWithAmount(resourceAmount));
        }
        tag.put(TAG_STACKS, stacks);
        return tag;
    }
}
