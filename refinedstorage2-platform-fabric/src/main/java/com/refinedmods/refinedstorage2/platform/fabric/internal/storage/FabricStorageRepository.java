package com.refinedmods.refinedstorage2.platform.fabric.internal.storage;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.StorageRepositoryImpl;
import com.refinedmods.refinedstorage2.api.storage.bulk.BulkStorage;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.bulk.StorageTypeAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.type.StorageTypeRegistry;

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

public class FabricStorageRepository extends PersistentState implements PlatformStorageRepository {
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
        markDirty();
    }

    @Override
    public <T> Optional<Storage<T>> disassemble(UUID id) {
        return parent.disassemble(id).map(storage -> {
            markDirty();
            return (BulkStorage<T>) storage;
        });
    }

    @Override
    public StorageInfo getInfo(UUID id) {
        return parent.getInfo(id);
    }

    public void read(NbtCompound tag) {
        NbtList storages = tag.getList(TAG_STORAGES, NbtType.COMPOUND);
        for (NbtElement storageTag : storages) {
            UUID id = ((NbtCompound) storageTag).getUuid(TAG_STORAGE_ID);
            Identifier typeIdentifier = new Identifier(((NbtCompound) storageTag).getString(TAG_STORAGE_TYPE));
            NbtCompound data = ((NbtCompound) storageTag).getCompound(TAG_STORAGE_DATA);

            StorageTypeRegistry.INSTANCE.getType(typeIdentifier).ifPresentOrElse(type -> {
                parent.set(id, type.fromTag(data, this));
            }, () -> {
                LOGGER.warn("Cannot find storage type {}", typeIdentifier);
            });
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        NbtList storageList = new NbtList();
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

    private NbtElement convertStorageToTag(UUID id, Storage<?> storage, StorageTypeAccessor typeAccessor) {
        Identifier typeIdentifier = StorageTypeRegistry
                .INSTANCE
                .getIdentifier(typeAccessor.getType())
                .orElseThrow(() -> new RuntimeException("Storage type is not registered"));

        NbtCompound tag = new NbtCompound();
        tag.putUuid(TAG_STORAGE_ID, id);
        tag.put(TAG_STORAGE_DATA, typeAccessor.getType().toTag(storage));
        tag.putString(TAG_STORAGE_TYPE, typeIdentifier.toString());
        return tag;
    }

    @Override
    public void markAsChanged() {
        markDirty();
    }
}
