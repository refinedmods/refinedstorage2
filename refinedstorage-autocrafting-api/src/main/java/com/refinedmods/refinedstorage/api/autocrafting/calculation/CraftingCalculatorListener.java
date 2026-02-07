package com.refinedmods.refinedstorage.api.autocrafting.calculation;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public interface CraftingCalculatorListener<T> {
    default void rootCalculationStarted(final ResourceKey resource, final long amount) {
    }

    CraftingCalculatorListener<T> childCalculationStarted(Pattern childPattern, ResourceKey resource, Amount amount);

    default void childCalculationCompleted(final CraftingCalculatorListener<T> childListener) {
    }

    default void ingredientsExhausted(final ResourceKey resource, final long amount) {
    }

    default void ingredientUsed(final Pattern ingredientPattern,
                                final int ingredientIndex,
                                final ResourceKey resource,
                                final long amount) {
    }

    default void ingredientExtractedFromStorage(final ResourceKey resource,
                                                final long amount) {
    }

    T getData();

    /**
     * {@return true if missing resource calculation must be complete}
     */
    default boolean requiresFullMissingResourcesCalculation() {
        return true;
    }
}
