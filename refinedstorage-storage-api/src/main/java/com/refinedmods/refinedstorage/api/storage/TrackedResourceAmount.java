package com.refinedmods.refinedstorage.api.storage;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.0")
public record TrackedResourceAmount(ResourceAmount resourceAmount, @Nullable TrackedResource trackedResource) {
}
