package com.refinedmods.refinedstorage2.platform.fabric.internal.storage.disk;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.PlatformItemStorageDisk;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.PlatformStorageDiskManager;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.StorageDiskType;
import com.refinedmods.refinedstorage2.platform.fabric.api.util.ItemStacks;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class ItemStorageDiskType implements StorageDiskType<Rs2ItemStack> {
    public static final ItemStorageDiskType INSTANCE = new ItemStorageDiskType();

    private static final String TAG_DISK_CAPACITY = "cap";
    private static final String TAG_DISK_STACKS = "stacks";

    private ItemStorageDiskType() {
    }

    @Override
    public StorageDisk<Rs2ItemStack> fromTag(NbtCompound tag, PlatformStorageDiskManager platformStorageDiskManager) {
        PlatformItemStorageDisk disk = new PlatformItemStorageDisk(tag.getLong(TAG_DISK_CAPACITY), platformStorageDiskManager::markAsChanged);
        NbtList stacks = tag.getList(TAG_DISK_STACKS, NbtType.COMPOUND);
        for (NbtElement stackTag : stacks) {
            Rs2ItemStack stack = ItemStacks.fromTag((NbtCompound) stackTag);
            if (stack.isEmpty()) {
                continue;
            }
            disk.insert(stack, stack.getAmount(), Action.EXECUTE);
        }
        return disk;
    }

    @Override
    public NbtCompound toTag(StorageDisk<Rs2ItemStack> disk) {
        NbtCompound tag = new NbtCompound();
        tag.putLong(TAG_DISK_CAPACITY, disk.getCapacity());
        NbtList stacks = new NbtList();
        for (Rs2ItemStack stack : disk.getStacks()) {
            stacks.add(ItemStacks.toTag(stack));
        }
        tag.put(TAG_DISK_STACKS, stacks);
        return tag;
    }
}
