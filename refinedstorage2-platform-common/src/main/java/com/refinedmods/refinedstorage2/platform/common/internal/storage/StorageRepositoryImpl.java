package com.refinedmods.refinedstorage2.platform.common.internal.storage;

import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageRepositoryImpl extends SavedData implements StorageRepository {
    public static final String NAME = "refinedstorage2_storages";

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageRepositoryImpl.class);

    private static final String TAG_STORAGES = "storages";
    private static final String TAG_STORAGE_ID = "id";
    private static final String TAG_STORAGE_TYPE = "type";
    private static final String TAG_STORAGE_DATA = "data";

    private final Map<UUID, Storage<?>> entries = new HashMap<>();
    private final OrderedRegistry<ResourceLocation, StorageType<?>> storageTypeRegistry;

    public StorageRepositoryImpl(final OrderedRegistry<ResourceLocation, StorageType<?>> storageTypeRegistry) {
        this.storageTypeRegistry = storageTypeRegistry;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<Storage<T>> get(final UUID id) {
        return Optional.ofNullable((Storage<T>) entries.get(id));
    }

    @Override
    public <T> void set(final UUID id, final Storage<T> storage) {
        setSilently(id, storage);
        setDirty();
    }

    private <T> void setSilently(final UUID id, final Storage<T> storage) {
        CoreValidations.validateNotNull(storage, "Storage must not be null");
        if (!(storage instanceof SerializableStorage<?>)) {
            throw new IllegalArgumentException("Storage is not serializable");
        }
        CoreValidations.validateNotNull(id, "ID must not be null");
        if (entries.containsKey(id)) {
            throw new IllegalArgumentException(id + " already exists");
        }
        entries.put(id, storage);
    }

    @Override
    public <T> Optional<Storage<T>> removeIfEmpty(final UUID id) {
        return this.<T>get(id).map(storage -> {
            if (storage.getStored() == 0) {
                entries.remove(id);
                setDirty();
                return storage;
            }
            return null;
        });
    }

    @Override
    public StorageInfo getInfo(final UUID id) {
        return get(id).map(StorageInfo::of).orElse(StorageInfo.UNKNOWN);
    }

    @Override
    public void markAsChanged() {
        setDirty();
    }

    public void read(final CompoundTag tag) {
        final ListTag storages = tag.getList(TAG_STORAGES, Tag.TAG_COMPOUND);
        for (final Tag storageTag : storages) {
            final UUID id = ((CompoundTag) storageTag).getUUID(TAG_STORAGE_ID);
            final ResourceLocation typeId = new ResourceLocation(
                ((CompoundTag) storageTag).getString(TAG_STORAGE_TYPE)
            );
            final CompoundTag data = ((CompoundTag) storageTag).getCompound(TAG_STORAGE_DATA);

            storageTypeRegistry.get(typeId).ifPresentOrElse(
                type -> setSilently(id, type.fromTag(data, this::markAsChanged)),
                () -> LOGGER.warn("Cannot find storage type {} for storage {}", typeId, id)
            );
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public CompoundTag save(final CompoundTag tag) {
        final ListTag storageList = new ListTag();
        for (final Map.Entry<UUID, Storage<?>> entry : entries.entrySet()) {
            if (entry.getValue() instanceof SerializableStorage serializableStorage) {
                storageList.add(convertStorageToTag(entry.getKey(), entry.getValue(), serializableStorage));
            } else {
                LOGGER.warn("Tried to persist non-serializable storage {}", entry.getKey());
            }
        }
        tag.put(TAG_STORAGES, storageList);
        return tag;
    }

    private <T> Tag convertStorageToTag(final UUID id, final Storage<T> storage,
                                        final SerializableStorage<T> serializableStorage) {
        final ResourceLocation typeIdentifier = storageTypeRegistry
            .getId(serializableStorage.getType())
            .orElseThrow(() -> new RuntimeException("Storage type is not registered"));

        final CompoundTag tag = new CompoundTag();
        tag.putUUID(TAG_STORAGE_ID, id);
        tag.put(TAG_STORAGE_DATA, serializableStorage.getType().toTag(storage));
        tag.putString(TAG_STORAGE_TYPE, typeIdentifier.toString());
        return tag;
    }
}
