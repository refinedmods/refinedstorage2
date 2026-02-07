package com.refinedmods.refinedstorage.common.storage;

import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.common.api.storage.StorageContents;
import com.refinedmods.refinedstorage.common.api.storage.StorageInfo;
import com.refinedmods.refinedstorage.common.api.storage.StorageRepository;
import com.refinedmods.refinedstorage.common.api.storage.StorageType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public class StorageRepositoryImpl extends SavedData implements StorageRepository {
    @SuppressWarnings("ConstantConditions") // Data fix type is null safe
    public static final SavedDataType<StorageRepositoryImpl> TYPE = new SavedDataType<>(
        createIdentifier("storages"),
        StorageRepositoryImpl::new,
        createCodec(),
        null
    );

    private final Map<UUID, SerializableStorage> entries;

    public StorageRepositoryImpl() {
        this(new HashMap<>());
    }

    public StorageRepositoryImpl(final Map<UUID, SerializableStorage> entries) {
        this.entries = entries;
    }

    private static Codec<StorageRepositoryImpl> createCodec() {
        final Codec<StorageContents> storageCodec = RefinedStorageApi.INSTANCE.getStorageTypeRegistry()
            .codec()
            .dispatch(StorageContents::type, StorageType::getCodec);
        return new ErrorHandlingMapCodec<>(UUIDUtil.STRING_CODEC, storageCodec)
            .xmap(entries -> {
                final StorageRepositoryImpl repository = new StorageRepositoryImpl();
                entries.forEach((id, contents) ->
                    repository.entries.put(id, contents.type().create(contents, repository::markAsChanged)));
                return repository;
            }, repository -> repository.entries.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().toContents()
            )));
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
                remove(id);
                return storage;
            }
            return null;
        });
    }

    @Override
    public void remove(final UUID id) {
        entries.remove(id);
        setDirty();
    }

    @Override
    public StorageInfo getInfo(final UUID id) {
        return get(id).map(StorageInfo::of).orElse(StorageInfo.UNKNOWN);
    }

    @Override
    public void markAsChanged() {
        setDirty();
    }
}
