package com.refinedmods.refinedstorage2.fabric.coreimpl.storage.disk;

import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskInfo;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskManager;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskManagerImpl;
import com.refinedmods.refinedstorage2.core.util.Action;
import com.refinedmods.refinedstorage2.fabric.util.ItemStacks;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.PersistentState;

public class FabricStorageDiskManager extends PersistentState implements StorageDiskManager {
    public static final String NAME = "refinedstorage2_disks";

    private static final String TAG_DISKS = "disks";
    private static final String TAG_DISK_ID = "id";
    private static final String TAG_DISK_CAPACITY = "cap";
    private static final String TAG_DISK_STACKS = "stacks";

    private final StorageDiskManagerImpl parent;

    public FabricStorageDiskManager(String name, StorageDiskManagerImpl parent) {
        super(name);
        this.parent = parent;
    }

    @Override
    public <T> Optional<StorageDisk<T>> getDisk(UUID id) {
        return parent.getDisk(id);
    }

    @Override
    public <T> void setDisk(UUID id, StorageDisk<T> disk) {
        parent.setDisk(id, disk);
        markDirty();
    }

    @Override
    public <T> Optional<StorageDisk<T>> disassembleDisk(UUID id) {
        return parent.disassembleDisk(id)
                .map(disk -> {
                    markDirty();
                    return (StorageDisk<T>) disk;
                });
    }

    @Override
    public StorageDiskInfo getInfo(UUID id) {
        return parent.getInfo(id);
    }

    @Override
    public void fromTag(CompoundTag tag) {
        ListTag disks = tag.getList(TAG_DISKS, NbtType.COMPOUND);
        for (Tag diskTag : disks) {
            UUID id = ((CompoundTag) diskTag).getUuid(TAG_DISK_ID);
            parent.setDisk(id, convertStorageDiskFromTag((CompoundTag) diskTag));
        }
    }

    private StorageDisk<?> convertStorageDiskFromTag(CompoundTag diskTag) {
        FabricItemDiskStorage disk = new FabricItemDiskStorage(diskTag.getLong(TAG_DISK_CAPACITY), this::markDirty);
        ListTag stacks = diskTag.getList(TAG_DISK_STACKS, NbtType.COMPOUND);
        for (Tag stackTag : stacks) {
            Rs2ItemStack stack = ItemStacks.fromTag((CompoundTag) stackTag);
            if (stack.isEmpty()) {
                continue;
            }
            disk.insert(stack, stack.getAmount(), Action.EXECUTE);
        }
        return disk;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        ListTag disksList = new ListTag();
        for (Map.Entry<UUID, StorageDisk<?>> entry : parent.getDisks()) {
            disksList.add(convertStorageDiskToTag(entry.getKey(), entry.getValue()));
        }
        tag.put(TAG_DISKS, disksList);
        return tag;
    }

    private Tag convertStorageDiskToTag(UUID id, StorageDisk<?> disk) {
        CompoundTag tag = new CompoundTag();
        tag.putUuid(TAG_DISK_ID, id);
        tag.putLong(TAG_DISK_CAPACITY, disk.getCapacity());
        ListTag stacks = new ListTag();
        for (Rs2ItemStack stack : (Collection<Rs2ItemStack>) disk.getStacks()) {
            stacks.add(ItemStacks.toTag(stack));
        }
        tag.put(TAG_DISK_STACKS, stacks);
        return tag;
    }
}
