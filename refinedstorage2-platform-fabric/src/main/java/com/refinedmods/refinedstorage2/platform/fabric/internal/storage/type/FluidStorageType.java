package com.refinedmods.refinedstorage2.platform.fabric.internal.storage.type;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.CapacityAccessor;
import com.refinedmods.refinedstorage2.api.storage.CappedStorage;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformCappedStorage;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorage;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.type.StorageType;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class FluidStorageType implements StorageType<FluidResource> {
    public static final FluidStorageType INSTANCE = new FluidStorageType();

    private static final String TAG_CAPACITY = "cap";
    private static final String TAG_STACKS = "stacks";

    private FluidStorageType() {
    }

    @Override
    public Storage<FluidResource> fromTag(CompoundTag tag, PlatformStorageRepository storageRepository) {
        Storage<FluidResource> storage = createStorage(tag, storageRepository);
        ListTag stacks = tag.getList(TAG_STACKS, NbtType.COMPOUND);
        for (Tag stackTag : stacks) {
            FluidResource.fromTagWithAmount((CompoundTag) stackTag).ifPresent(resourceAmount -> storage.insert(resourceAmount.getResource(), resourceAmount.getAmount(), Action.EXECUTE));
        }
        return storage;
    }

    private Storage<FluidResource> createStorage(CompoundTag tag, PlatformStorageRepository storageRepository) {
        if (tag.contains(TAG_CAPACITY)) {
            return new PlatformCappedStorage<>(
                    new CappedStorage<>(tag.getLong(TAG_CAPACITY)),
                    FluidStorageType.INSTANCE,
                    storageRepository::markAsChanged
            );
        }
        return new PlatformStorage<>(
                new InMemoryStorageImpl<>(),
                FluidStorageType.INSTANCE,
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
            stacks.add(FluidResource.toTagWithAmount(resourceAmount));
        }
        tag.put(TAG_STACKS, stacks);
        return tag;
    }
}
