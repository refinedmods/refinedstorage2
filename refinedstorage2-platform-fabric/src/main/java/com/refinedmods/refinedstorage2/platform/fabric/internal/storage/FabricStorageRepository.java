package com.refinedmods.refinedstorage2.platform.fabric.internal.storage;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.StorageRepositoryImpl;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.StorageTypeAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.type.StorageTypeRegistry;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FabricStorageRepository extends SavedData implements PlatformStorageRepository {
    public static final String NAME = "refinedstorage2_storages";

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String TAG_STORAGES = "storages";
    private static final String TAG_STORAGE_ID = "id";
    private static final String TAG_STORAGE_TYPE = "type";
    private static final String TAG_STORAGE_DATA = "data";

    private final StorageRepositoryImpl parent;

    public FabricStorageRepository(StorageRepositoryImpl parent) {
        this.parent = parent;
    }

    @Override
    public <T> Optional<Storage<T>> get(UUID id) {
        return parent.get(id);
    }

    @Override
    public <T> void set(UUID id, Storage<T> storage) {
        parent.set(id, storage);
        setDirty();
    }

    @Override
    public <T> Optional<Storage<T>> disassemble(UUID id) {
        return parent.disassemble(id).map(storage -> {
            setDirty();
            return (Storage<T>) storage;
        });
    }

    @Override
    public StorageInfo getInfo(UUID id) {
        return parent.getInfo(id);
    }

    public void read(CompoundTag tag) {
        ListTag storages = tag.getList(TAG_STORAGES, NbtType.COMPOUND);
        for (Tag storageTag : storages) {
            UUID id = ((CompoundTag) storageTag).getUUID(TAG_STORAGE_ID);
            ResourceLocation typeIdentifier = new ResourceLocation(((CompoundTag) storageTag).getString(TAG_STORAGE_TYPE));
            CompoundTag data = ((CompoundTag) storageTag).getCompound(TAG_STORAGE_DATA);

            StorageTypeRegistry.INSTANCE.getType(typeIdentifier).ifPresentOrElse(type -> {
                parent.set(id, type.fromTag(data, this));
            }, () -> {
                LOGGER.warn("Cannot find storage type {}", typeIdentifier);
            });
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag storageList = new ListTag();
        for (Map.Entry<UUID, Storage<?>> entry : parent.getAll()) {
            if (entry.getValue() instanceof StorageTypeAccessor storageTypeAccessor) {
                storageList.add(convertStorageToTag(entry.getKey(), entry.getValue(), storageTypeAccessor));
            } else {
                LOGGER.warn("Tried to persist non-platform storage {}", entry.getKey());
            }
        }
        tag.put(TAG_STORAGES, storageList);
        return tag;
    }

    private Tag convertStorageToTag(UUID id, Storage<?> storage, StorageTypeAccessor typeAccessor) {
        ResourceLocation typeIdentifier = StorageTypeRegistry
                .INSTANCE
                .getIdentifier(typeAccessor.getType())
                .orElseThrow(() -> new RuntimeException("Storage type is not registered"));

        CompoundTag tag = new CompoundTag();
        tag.putUUID(TAG_STORAGE_ID, id);
        tag.put(TAG_STORAGE_DATA, typeAccessor.getType().toTag(storage));
        tag.putString(TAG_STORAGE_TYPE, typeIdentifier.toString());
        return tag;
    }

    @Override
    public void markAsChanged() {
        setDirty();
    }
}
