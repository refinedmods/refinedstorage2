package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.0")
public record TrackedResourceAmount<T>(ResourceAmount<T> resourceAmount, @Nullable TrackedResource trackedResource) {
}
