package com.refinedmods.refinedstorage.api.storage;

import org.apiguardian.api.API;

/**
 * Access mode of a storage.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public enum AccessMode {
    /**
     * Insertions and extractions.
     */
    INSERT_EXTRACT,
    /**
     * Insert-only. Storage contents are still visible in the Grid.
     */
    INSERT,
    /**
     * Extract-only.
     */
    EXTRACT;

    public boolean isInsertOnly() {
        return this == INSERT;
    }

    public boolean isExtractOnly() {
        return this == EXTRACT;
    }
}
