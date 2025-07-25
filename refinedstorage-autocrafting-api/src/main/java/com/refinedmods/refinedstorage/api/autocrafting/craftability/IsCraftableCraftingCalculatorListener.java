package com.refinedmods.refinedstorage.api.autocrafting.craftability;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.Amount;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CalculationException;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationException;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculator;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculatorListener;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public class IsCraftableCraftingCalculatorListener implements CraftingCalculatorListener<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(IsCraftableCraftingCalculatorListener.class);

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
            LOGGER.debug("Finding max amount for {} starting from 1", resource);
            long low = 1;
            long high = 1;
            int calculationCount = 1;
            while (isCraftable(calculator, resource, high, cancellationToken)) {
                low = high;
                high = high * 2;
                LOGGER.debug("Finding low and high for the craftable amount, currently between {} and {}", low, high);
                calculationCount++;
            }
            if (low == high) {
                return 0;
            }
            LOGGER.debug("Our craftable amount is between {} and {}", low, high);
            while (low < high) {
                final long amount = low + (high - low + 1) / 2;
                LOGGER.debug("Trying {} (between {} and {})", amount, low, high);
                calculationCount++;
                if (isCraftable(calculator, resource, amount, cancellationToken)) {
                    LOGGER.debug("{} was craftable, increasing our low amount", amount);
                    low = amount;
                } else {
                    LOGGER.debug("{} is not craftable, decreasing our high amount", amount);
                    high = amount - 1;
                }
            }
            LOGGER.debug("Found the maximum amount of {} in {} tries", low, calculationCount);
            return low;
        } catch (final CalculationException e) {
            LOGGER.error("Error while calculating max amount for {}", resource, e);
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
