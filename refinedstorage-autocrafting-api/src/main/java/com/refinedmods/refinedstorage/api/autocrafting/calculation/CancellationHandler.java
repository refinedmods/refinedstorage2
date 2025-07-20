package com.refinedmods.refinedstorage.api.autocrafting.calculation;

import org.apiguardian.api.API;

@FunctionalInterface
@API(status = API.Status.STABLE, since = "2.0.0-beta.3")
public interface CancellationHandler {
    CancellationHandler NONE = () -> false;

    boolean isCancelled();
}
