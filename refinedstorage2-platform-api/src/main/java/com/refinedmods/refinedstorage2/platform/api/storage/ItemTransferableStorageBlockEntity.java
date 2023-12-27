package com.refinedmods.refinedstorage2.platform.api.storage;

import java.util.UUID;
import javax.annotation.Nullable;

import org.apiguardian.api.API;

/**
 * Implement this on a block entity that contains a storage (ID) that can be transferred to/from an item.
 * The "storage block" is an example of such an item.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.3")
public interface ItemTransferableStorageBlockEntity {
    @Nullable
    UUID getStorageId();

    void modifyStorageIdAfterAlreadyInitialized(UUID id);
}
