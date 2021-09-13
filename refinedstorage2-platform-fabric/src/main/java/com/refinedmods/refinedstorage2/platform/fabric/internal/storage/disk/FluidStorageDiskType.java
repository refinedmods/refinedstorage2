package com.refinedmods.refinedstorage2.platform.fabric.internal.storage.disk;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.api.stack.list.StackListImpl;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.PlatformStorageDiskImpl;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.PlatformStorageDiskManager;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.StorageDiskType;
import com.refinedmods.refinedstorage2.platform.fabric.api.util.FluidStacks;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class FluidStorageDiskType implements StorageDiskType<Rs2FluidStack> {
    public static final FluidStorageDiskType INSTANCE = new FluidStorageDiskType();

    private static final String TAG_DISK_CAPACITY = "cap";
    private static final String TAG_DISK_STACKS = "stacks";

    private FluidStorageDiskType() {
    }

    @Override
    public StorageDisk<Rs2FluidStack> fromTag(NbtCompound tag, PlatformStorageDiskManager platformStorageDiskManager) {
        StorageDisk<Rs2FluidStack> disk = new PlatformStorageDiskImpl<>(
                tag.getLong(TAG_DISK_CAPACITY),
                StackListImpl.createFluidStackList(),
                FluidStorageDiskType.INSTANCE,
                platformStorageDiskManager::markAsChanged
        );

        NbtList stacks = tag.getList(TAG_DISK_STACKS, NbtType.COMPOUND);
        for (NbtElement stackTag : stacks) {
            Rs2FluidStack stack = FluidStacks.fromTag((NbtCompound) stackTag);
            if (stack.isEmpty()) {
                continue;
            }
            disk.insert(stack, stack.getAmount(), Action.EXECUTE);
        }
        return disk;
    }

    @Override
    public NbtCompound toTag(StorageDisk<Rs2FluidStack> disk) {
        NbtCompound tag = new NbtCompound();
        tag.putLong(TAG_DISK_CAPACITY, disk.getCapacity());
        NbtList stacks = new NbtList();
        for (Rs2FluidStack stack : disk.getAll()) {
            stacks.add(FluidStacks.toTag(stack));
        }
        tag.put(TAG_DISK_STACKS, stacks);
        return tag;
    }
}
