package com.refinedmods.refinedstorage2.platform.api.resource;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.3")
public interface FuzzyModeNormalizer<T> {
    T normalize();
}
