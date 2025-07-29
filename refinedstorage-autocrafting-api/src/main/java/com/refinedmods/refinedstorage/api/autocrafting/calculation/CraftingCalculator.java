package com.refinedmods.refinedstorage.api.autocrafting.calculation;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
@FunctionalInterface
public interface CraftingCalculator {
    <T> void calculate(ResourceKey resource, long amount, CraftingCalculatorListener<T> listener,
                       CancellationToken cancellationToken) throws CancellationException;
}
