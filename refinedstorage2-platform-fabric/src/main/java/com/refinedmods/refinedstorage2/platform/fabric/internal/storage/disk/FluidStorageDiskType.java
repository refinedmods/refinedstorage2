package com.refinedmods.refinedstorage2.platform.fabric.internal.storage.disk;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.PlatformStorageDiskImpl;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.PlatformStorageDiskManager;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.StorageDiskType;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class FluidStorageDiskType implements StorageDiskType<FluidResource> {
    public static final FluidStorageDiskType INSTANCE = new FluidStorageDiskType();

    private static final String TAG_DISK_CAPACITY = "cap";
    private static final String TAG_DISK_STACKS = "stacks";

    private FluidStorageDiskType() {
    }

    @Override
    public StorageDisk<FluidResource> fromTag(NbtCompound tag, PlatformStorageDiskManager platformStorageDiskManager) {
        StorageDisk<FluidResource> disk = new PlatformStorageDiskImpl<>(
                tag.getLong(TAG_DISK_CAPACITY),
                FluidStorageDiskType.INSTANCE,
                platformStorageDiskManager::markAsChanged
        );

        NbtList stacks = tag.getList(TAG_DISK_STACKS, NbtType.COMPOUND);
        for (NbtElement stackTag : stacks) {
            FluidResource.fromTagWithAmount((NbtCompound) stackTag).ifPresent(resourceAmount -> disk.insert(resourceAmount.getResource(), resourceAmount.getAmount(), Action.EXECUTE));
        }
        return disk;
    }

    @Override
    public NbtCompound toTag(StorageDisk<FluidResource> disk) {
        NbtCompound tag = new NbtCompound();
        tag.putLong(TAG_DISK_CAPACITY, disk.getCapacity());
        NbtList stacks = new NbtList();
        for (ResourceAmount<FluidResource> resourceAmount : disk.getAll()) {
            stacks.add(FluidResource.toTagWithAmount(resourceAmount));
        }
        tag.put(TAG_DISK_STACKS, stacks);
        return tag;
    }
}
