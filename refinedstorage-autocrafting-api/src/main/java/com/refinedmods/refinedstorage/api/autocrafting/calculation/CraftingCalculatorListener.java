package com.refinedmods.refinedstorage.api.autocrafting.calculation;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public interface CraftingCalculatorListener<T> {
    default void rootCalculationStarted(ResourceKey resource, long amount) {
    }

    CraftingCalculatorListener<T> childCalculationStarted(Pattern childPattern, ResourceKey resource, Amount amount);

    default void childCalculationCompleted(CraftingCalculatorListener<T> childListener) {
    }

    default void ingredientsExhausted(ResourceKey resource, long amount) {
    }

    default void ingredientUsed(Pattern ingredientPattern, int ingredientIndex, ResourceKey resource, long amount) {
    }

    default void ingredientExtractedFromStorage(ResourceKey resource, long amount) {
    }

    T getData();

    /**
     * {@return true if missing resource calculation must be complete}
     */
    default boolean requiresFullMissingResourcesCalculation() {
        return true;
    }
}
