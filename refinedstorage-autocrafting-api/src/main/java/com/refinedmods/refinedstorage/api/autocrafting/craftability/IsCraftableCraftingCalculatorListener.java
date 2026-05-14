package com.refinedmods.refinedstorage.api.autocrafting.craftability;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.Amount;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CalculationException;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationException;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculator;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculatorListener;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import static java.util.Objects.requireNonNull;

public class IsCraftableCraftingCalculatorListener implements CraftingCalculatorListener<Boolean> {
    private boolean missingResources;

    private IsCraftableCraftingCalculatorListener() {
    }

    private IsCraftableCraftingCalculatorListener(final boolean missingResources) {
        this.missingResources = missingResources;
    }

    private static boolean isCraftable(final CraftingCalculator calculator,
                                       final ResourceKey resource,
                                       final long amount,
                                       final CancellationToken cancellationToken) {
        final IsCraftableCraftingCalculatorListener listener = new IsCraftableCraftingCalculatorListener();
        try {
            calculator.calculate(resource, amount, listener, cancellationToken);
        } catch (final CancellationException e) {
            return false;
        }
        return !listener.missingResources;
    }

    public static long binarySearchMaxAmount(final CraftingCalculator calculator,
                                             final ResourceKey resource,
                                             final CancellationToken cancellationToken) {
        try {
            long low = 1;
            long high = 1;
            while (isCraftable(calculator, resource, high, cancellationToken)) {
                low = high;
                high = high * 2;
            }
            if (low == high) {
                return 0;
            }
            while (low < high) {
                final long amount = low + (high - low + 1) / 2;
                if (isCraftable(calculator, resource, amount, cancellationToken)) {
                    low = amount;
                } else {
                    high = amount - 1;
                }
            }
            return low;
        } catch (final CalculationException e) {
            return 0;
        }
    }

    @Override
    public CraftingCalculatorListener<Boolean> childCalculationStarted(final Pattern childPattern,
                                                                       final ResourceKey resource,
                                                                       final Amount amount) {
        return new IsCraftableCraftingCalculatorListener(missingResources);
    }

    @Override
    public void childCalculationCompleted(final CraftingCalculatorListener<Boolean> childListener) {
        missingResources = requireNonNull(childListener.getData());
    }

    @Override
    public void ingredientsExhausted(final ResourceKey resource, final long amount) {
        missingResources = true;
    }

    @Override
    public Boolean getData() {
        return missingResources;
    }

    @Override
    public boolean requiresFullMissingResourcesCalculation() {
        return false;
    }
}
