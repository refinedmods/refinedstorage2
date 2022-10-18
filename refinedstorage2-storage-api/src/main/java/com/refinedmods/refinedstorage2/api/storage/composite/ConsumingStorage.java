package com.refinedmods.refinedstorage2.api.storage.composite;

import org.apiguardian.api.API;

/**
 * Marker interface for "consuming" storages.
 * Consuming storages are storages that accept resources, but don't expect results from operations
 * to be reflected in the composite storage cached list.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public interface ConsumingStorage {
}
