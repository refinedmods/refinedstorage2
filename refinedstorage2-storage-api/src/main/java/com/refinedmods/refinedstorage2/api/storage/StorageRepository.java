package com.refinedmods.refinedstorage2.api.storage;

import java.util.Optional;
import java.util.UUID;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public interface StorageRepository {
    /**
     * Retrieves a storage by ID, if it exists.
     *
     * @param id  the id
     * @param <T> the resource type
     * @return the storage, if present
     */
    <T> Optional<Storage<T>> get(UUID id);

    /**
     * Sets a storage by ID.
     *
     * @param id      the id
     * @param storage the storage
     * @param <T>     the resource type
     */
    <T> void set(UUID id, Storage<T> storage);

    /**
     * Tries to disassemble the storage.
     * If the storage exists, and is empty, it must remove the storage from the repository.
     *
     * @param id  the id
     * @param <T> the resource type
     * @return the removed storage, if it existed and was empty
     */
    <T> Optional<Storage<T>> disassemble(UUID id); // TODO: Rename to removeIfEmpty

    /**
     * Retrieves info for a given storage ID.
     *
     * @param id the id
     * @return the info, or {@link StorageInfo#UNKNOWN} if the ID was not found
     */
    StorageInfo getInfo(UUID id);
}
