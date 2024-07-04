package com.refinedmods.refinedstorage.platform.api.storage;

import java.util.Optional;
import java.util.UUID;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public interface StorageRepository {
    /**
     * Retrieves a storage by ID, if it exists.
     *
     * @param id the id
     * @return the storage, if present
     */
    Optional<SerializableStorage> get(UUID id);

    /**
     * Sets a storage by ID.
     *
     * @param id      the id
     * @param storage the storage
     */
    void set(UUID id, SerializableStorage storage);

    /**
     * If the storage exists, and is empty, it will remove the storage from the repository.
     *
     * @param id the id
     * @return the removed storage, if it existed and was empty
     */
    Optional<SerializableStorage> removeIfEmpty(UUID id);

    /**
     * Retrieves info for a given storage ID.
     *
     * @param id the id
     * @return the info, or {@link StorageInfo#UNKNOWN} if the ID was not found
     */
    StorageInfo getInfo(UUID id);

    /**
     * Marks to be saved.
     */
    void markAsChanged();
}
