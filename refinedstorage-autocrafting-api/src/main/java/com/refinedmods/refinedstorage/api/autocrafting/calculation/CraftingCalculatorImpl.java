package com.refinedmods.refinedstorage.api.autocrafting.calculation;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternRepository;
import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingTree.root;

public class CraftingCalculatorImpl implements CraftingCalculator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CraftingCalculatorImpl.class);

    private final PatternRepository patternRepository;
    private final RootStorage rootStorage;

    public CraftingCalculatorImpl(final PatternRepository patternRepository, final RootStorage rootStorage) {
        this.patternRepository = patternRepository;
        this.rootStorage = rootStorage;
    }

    @Override
    public <T> void calculate(final ResourceKey resource,
                              final long amount,
                              final CraftingCalculatorListener<T> listener,
                              final CancellationToken cancellationToken) {
        CoreValidations.validateLargerThanZero(amount, "Requested amount must be greater than 0");
        final Collection<Pattern> patterns = patternRepository.getByOutput(resource);
        CraftingCalculatorListener<T> lastChildListener = null;
        for (final Pattern pattern : patterns) {
            final Amount patternAmount = Amount.of(pattern, resource, amount);
            if (patternAmount.getTotal() < 0) {
                throw new NumberOverflowDuringCalculationException();
            }
            final CraftingCalculatorListener<T> childListener = listener.childCalculationStarted(
                pattern,
                resource,
                patternAmount
            );
            final CraftingTree<T> tree = root(pattern, rootStorage, patternAmount, patternRepository, childListener);
            final CraftingTree.CalculationResult calculationResult = tree.calculate(cancellationToken);
            if (calculationResult == CraftingTree.CalculationResult.MISSING_RESOURCES) {
                lastChildListener = childListener;
                continue;
            } else if (calculationResult == CraftingTree.CalculationResult.CANCELLED) {
                listener.childCalculationCancelled(childListener);
                return;
            }
            listener.childCalculationCompleted(childListener);
            return;
        }
        if (lastChildListener == null) {
            throw new IllegalStateException("No pattern found for " + resource);
        }
        listener.childCalculationCompleted(lastChildListener);
    }

    private boolean isCraftable(final ResourceKey resource, final long amount,
                                final CancellationToken cancellationToken) {
        final MissingResourcesCraftingCalculatorListener listener = new MissingResourcesCraftingCalculatorListener();
        calculate(resource, amount, listener, cancellationToken);
        return !listener.isMissingResources();
    }

    @Override
    public long getMaxAmount(final ResourceKey resource, final CancellationToken cancellationToken) {
        try {
            LOGGER.debug("Finding max amount for {} starting from 1", resource);
            long low = 1;
            long high = 1;
            int calculationCount = 1;
            while (isCraftable(resource, high, cancellationToken)) {
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
                if (isCraftable(resource, amount, cancellationToken)) {
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
            LOGGER.debug("Failed to calculate the maximum amount", e);
            return 0;
        }
    }
}
