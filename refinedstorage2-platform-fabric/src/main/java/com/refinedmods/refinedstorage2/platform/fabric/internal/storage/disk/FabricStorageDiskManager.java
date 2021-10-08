package com.refinedmods.refinedstorage2.platform.fabric.internal.storage.disk;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDiskManagerImpl;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.PlatformStorageDisk;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.PlatformStorageDiskManager;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.StorageDiskTypeRegistry;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FabricStorageDiskManager extends PersistentState implements PlatformStorageDiskManager {
    public static final String NAME = "refinedstorage2_disks2";

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String TAG_DISKS = "disks";
    private static final String TAG_DISK_ID = "id";
    private static final String TAG_DISK_TYPE = "type";
    private static final String TAG_DISK_DATA = "data";

    private final StorageDiskManagerImpl parent;

    public FabricStorageDiskManager(StorageDiskManagerImpl parent) {
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
    public StorageInfo getInfo(UUID id) {
        return parent.getInfo(id);
    }

    public void read(NbtCompound tag) {
        NbtList disks = tag.getList(TAG_DISKS, NbtType.COMPOUND);
        for (NbtElement diskTag : disks) {
            UUID id = ((NbtCompound) diskTag).getUuid(TAG_DISK_ID);
            Identifier typeIdentifier = new Identifier(((NbtCompound) diskTag).getString(TAG_DISK_TYPE));
            NbtCompound data = ((NbtCompound) diskTag).getCompound(TAG_DISK_DATA);

            StorageDiskTypeRegistry.INSTANCE.getType(typeIdentifier).ifPresentOrElse(type -> {
                parent.setDisk(id, type.fromTag(data, this));
            }, () -> {
                LOGGER.warn("Cannot find storage disk type {}", typeIdentifier);
            });
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        NbtList disksList = new NbtList();
        for (Map.Entry<UUID, StorageDisk<?>> entry : parent.getDisks()) {
            if (entry.getValue() instanceof PlatformStorageDisk<?> platformStorageDisk) {
                disksList.add(convertStorageDiskToTag(entry.getKey(), platformStorageDisk));
            } else {
                LOGGER.warn("Tried to persist non-platform storage disk {}", entry.getKey());
            }
        }
        tag.put(TAG_DISKS, disksList);
        return tag;
    }

    private NbtElement convertStorageDiskToTag(UUID id, PlatformStorageDisk<?> disk) {
        Identifier typeIdentifier = StorageDiskTypeRegistry.INSTANCE.getIdentifier(disk.getType()).orElseThrow(() -> new RuntimeException("Disk type is not registered"));

        NbtCompound tag = new NbtCompound();
        tag.putUuid(TAG_DISK_ID, id);
        tag.put(TAG_DISK_DATA, disk.getType().toTag((PlatformStorageDisk) disk));
        tag.putString(TAG_DISK_TYPE, typeIdentifier.toString());
        return tag;
    }

    @Override
    public void markAsChanged() {
        markDirty();
    }
}
