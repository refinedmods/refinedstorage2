package com.refinedmods.refinedstorage.api.autocrafting.calculation;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public interface CraftingCalculatorListener<T> {
    CraftingCalculatorListener<T> childCalculationStarted(Pattern childPattern, ResourceKey resource, Amount amount);

    void childCalculationCompleted(CraftingCalculatorListener<T> childListener);

    void childCalculationCancelled(CraftingCalculatorListener<T> childListener);

    void ingredientsExhausted(ResourceKey resource, long amount);

    void ingredientUsed(Pattern ingredientPattern, int ingredientIndex, ResourceKey resource, long amount);

    void ingredientExtractedFromStorage(ResourceKey resource, long amount);

    T getData();
}
