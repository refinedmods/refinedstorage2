package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import org.apiguardian.api.API;

/**
 * A resource template adds storage channel information to a resource.
 *
 * @param resource           the resource
 * @param storageChannelType the storage channel type
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.13")
public record ResourceTemplate(ResourceKey resource, StorageChannelType storageChannelType) {
}
