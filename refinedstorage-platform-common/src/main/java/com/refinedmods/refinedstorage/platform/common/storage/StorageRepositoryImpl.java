package com.refinedmods.refinedstorage.platform.common.storage;

import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.platform.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.platform.api.storage.StorageInfo;
import com.refinedmods.refinedstorage.platform.api.storage.StorageRepository;
import com.refinedmods.refinedstorage.platform.common.support.AbstractSafeSavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

public class StorageRepositoryImpl extends AbstractSafeSavedData implements StorageRepository {
    public static final String NAME = "refinedstorage_storages";

    private final Codec<Map<UUID, SerializableStorage>> codec = Codec.unboundedMap(
        UUIDUtil.STRING_CODEC,
        SerializableStorage.getCodec(this::markAsChanged)
    );
    private final Map<UUID, SerializableStorage> entries;

    public StorageRepositoryImpl(final CompoundTag tag, final HolderLookup.Provider provider) {
        this.entries = new HashMap<>(codec.decode(
            provider.createSerializationContext(NbtOps.INSTANCE),
            tag
        ).getOrThrow().getFirst());
    }

    public StorageRepositoryImpl() {
        this.entries = new HashMap<>();
    }

    @Override
    public Optional<SerializableStorage> get(final UUID id) {
        return Optional.ofNullable(entries.get(id));
    }

    @Override
    public void set(final UUID id, final SerializableStorage storage) {
        CoreValidations.validateNotNull(storage, "Storage must not be null");
        CoreValidations.validateNotNull(id, "ID must not be null");
        if (entries.containsKey(id)) {
            throw new IllegalArgumentException(id + " already exists");
        }
        entries.put(id, storage);
        setDirty();
    }

    @Override
    public Optional<SerializableStorage> removeIfEmpty(final UUID id) {
        return get(id).map(storage -> {
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

    @Override
    public CompoundTag save(final CompoundTag tag, final HolderLookup.Provider provider) {
        return (CompoundTag) codec.encode(entries, provider.createSerializationContext(NbtOps.INSTANCE), tag)
            .getOrThrow();
    }
}
