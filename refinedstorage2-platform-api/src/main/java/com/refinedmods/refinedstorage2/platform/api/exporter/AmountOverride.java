package com.refinedmods.refinedstorage2.platform.api.exporter;

import com.refinedmods.refinedstorage2.api.resource.ResourceKey;

import java.util.function.LongSupplier;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.12")
@FunctionalInterface
public interface AmountOverride {
    AmountOverride NONE = (resource, amount, currentAmount) -> amount;

    /**
     * Modifies the requested amount to a new amount.
     *
     * @param resource      the resource
     * @param amount        the original requested amount
     * @param currentAmount the current amount present in the source
     * @return the new requested amount, may be 0
     */
    long overrideAmount(ResourceKey resource, long amount, LongSupplier currentAmount);
}
