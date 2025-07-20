package com.refinedmods.refinedstorage.api.autocrafting.calculation;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import static java.util.Objects.requireNonNull;

class MissingResourcesCraftingCalculatorListener implements CraftingCalculatorListener<Boolean> {
    private boolean missingResources;

    MissingResourcesCraftingCalculatorListener() {
    }

    MissingResourcesCraftingCalculatorListener(final boolean missingResources) {
        this.missingResources = missingResources;
    }

    boolean isMissingResources() {
        return missingResources;
    }

    @Override
    public CraftingCalculatorListener<Boolean> childCalculationStarted(final Pattern childPattern,
                                                                       final ResourceKey resource,
                                                                       final Amount amount) {
        return new MissingResourcesCraftingCalculatorListener(missingResources);
    }

    @Override
    public void childCalculationCompleted(final CraftingCalculatorListener<Boolean> childListener) {
        missingResources = requireNonNull(childListener.getData());
    }

    @Override
    public void childCalculationCancelled(final CraftingCalculatorListener<Boolean> childListener) {
        missingResources = true;
    }

    @Override
    public void ingredientsExhausted(final ResourceKey resource, final long amount) {
        missingResources = true;
    }

    @Override
    public void ingredientUsed(final Pattern ingredientPattern,
                               final int ingredientIndex,
                               final ResourceKey resource,
                               final long amount) {
        // no op
    }

    @Override
    public void ingredientExtractedFromStorage(final ResourceKey resource, final long amount) {
        // no op
    }

    @Override
    public Boolean getData() {
        return missingResources;
    }
}
