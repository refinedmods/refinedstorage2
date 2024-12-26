package com.refinedmods.refinedstorage.api.autocrafting.calculation;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternRepository;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.List;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

class CraftingTree<T> {
    private final Pattern pattern;
    private final Amount amount;
    private final PatternRepository patternRepository;
    private final CraftingCalculatorListener<T> listener;
    private CraftingState craftingState;

    private CraftingTree(final Pattern pattern,
                         final CraftingState craftingState,
                         final Amount amount,
                         final PatternRepository patternRepository,
                         final CraftingCalculatorListener<T> listener) {
        this.pattern = pattern;
        this.craftingState = craftingState;
        this.amount = amount;
        this.patternRepository = patternRepository;
        this.listener = listener;
    }

    static <T> CraftingTree<T> root(final Pattern pattern,
                                    final RootStorage rootStorage,
                                    final Amount amount,
                                    final PatternRepository patternRepository,
                                    final CraftingCalculatorListener<T> listener) {
        final CraftingState craftingState = CraftingState.of(rootStorage);
        return new CraftingTree<>(pattern, craftingState, amount, patternRepository, listener);
    }

    static <T> CraftingTree<T> child(final Pattern pattern,
                                     final CraftingState parentState,
                                     final Amount amount,
                                     final PatternRepository patternRepository,
                                     final CraftingCalculatorListener<T> listener) {
        return new CraftingTree<>(pattern, parentState.copy(), amount, patternRepository,
            listener.childCalculationStarted());
    }

    CalculationResult calculate() {
        CalculationResult result = CalculationResult.SUCCESS;
        for (final Ingredient ingredient : pattern.getIngredients()) {
            if (ingredient.isEmpty()) {
                continue;
            }
            final IngredientState ingredientState = new IngredientState(ingredient, craftingState);
            final CalculationResult ingredientResult = calculateIngredient(ingredientState);
            if (ingredientResult == CalculationResult.MISSING_RESOURCES) {
                result = CalculationResult.MISSING_RESOURCES;
            }
        }
        craftingState.addOutputsToInternalStorage(pattern, amount);
        return result;
    }

    private CalculationResult calculateIngredient(final IngredientState ingredientState) {
        CraftingState.ResourceState resourceState = craftingState.getResource(ingredientState.get());
        long remaining = ingredientState.amount() * amount.iterations();
        while (remaining > 0) {
            if (resourceState.isInInternalStorage()) {
                final long toTake = Math.min(remaining, resourceState.inInternalStorage());
                craftingState.extractFromInternalStorage(resourceState.resource(), toTake);
                remaining -= toTake;
            }
            if (remaining > 0 && resourceState.isInStorage()) {
                final long toTake = Math.min(remaining, resourceState.inStorage());
                craftingState.extractFromStorage(resourceState.resource(), toTake);
                listener.ingredientExtractedFromStorage(resourceState.resource(), toTake);
                remaining -= toTake;
            }
            if (remaining > 0) {
                final CraftingState.ResourceState newState = tryCalculateChild(
                    ingredientState,
                    resourceState,
                    remaining
                );
                if (newState == null) {
                    // If we end up with missing resources, we need to use up all the resulting
                    // resources from the internal storage so that it cannot be used later for other ingredients
                    // that happen to use the same resource.
                    // The goal was to use up the resources created by the child calculation in the next iteration,
                    // but since we have missing resources, we need to use them up now.
                    craftingState.extractFromInternalStorage(resourceState.resource(), remaining);
                    return CalculationResult.MISSING_RESOURCES;
                } else {
                    resourceState = newState;
                }
            }
        }
        return CalculationResult.SUCCESS;
    }

    @Nullable
    private CraftingState.ResourceState tryCalculateChild(final IngredientState ingredientState,
                                                          final CraftingState.ResourceState resourceState,
                                                          final long remaining) {
        final List<Pattern> childPatterns = patternRepository.getByOutput(resourceState.resource());
        if (!childPatterns.isEmpty()) {
            return calculateChild(
                ingredientState,
                remaining,
                childPatterns,
                resourceState
            );
        }
        return ingredientState.cycle().map(craftingState::getResource).orElseGet(() -> {
            listener.ingredientsExhausted(resourceState.resource(), remaining);
            return null;
        });
    }

    @Nullable
    private CraftingState.ResourceState calculateChild(final IngredientState ingredientState,
                                                       final long remaining,
                                                       final List<Pattern> childPatterns,
                                                       final CraftingState.ResourceState resourceState) {
        final ChildCalculationResult<T> result = calculateChild(remaining, childPatterns, resourceState);
        if (result.success) {
            this.craftingState = result.childTree.craftingState;
            final CraftingState.ResourceState updatedResourceState = craftingState.getResource(
                resourceState.resource()
            );
            listener.childCalculationCompleted(
                updatedResourceState.resource(),
                updatedResourceState.inInternalStorage(),
                result.childTree.listener
            );
            return updatedResourceState;
        }
        return cycleToNextIngredientOrFail(ingredientState, resourceState, result);
    }

    private ChildCalculationResult<T> calculateChild(final long remaining,
                                                     final List<Pattern> childPatterns,
                                                     final CraftingState.ResourceState resourceState) {
        CraftingTree<T> lastChildTree = null;
        Amount lastChildAmount = null;
        for (final Pattern childPattern : childPatterns) {
            final Amount childAmount = Amount.of(childPattern, resourceState.resource(), remaining);
            final CraftingTree<T> childTree = child(
                childPattern,
                craftingState,
                childAmount,
                patternRepository,
                listener
            );
            final CalculationResult childResult = childTree.calculate();
            if (childResult == CalculationResult.MISSING_RESOURCES) {
                lastChildTree = childTree;
                lastChildAmount = childAmount;
                continue;
            }
            return new ChildCalculationResult<>(
                true,
                craftingState.getResource(resourceState.resource()).inInternalStorage(),
                childTree
            );
        }
        return new ChildCalculationResult<>(
            false,
            requireNonNull(lastChildAmount).getTotal(),
            requireNonNull(lastChildTree)
        );
    }

    @Nullable
    private CraftingState.ResourceState cycleToNextIngredientOrFail(final IngredientState ingredientState,
                                                                    final CraftingState.ResourceState resourceState,
                                                                    final ChildCalculationResult<T> childResult) {
        return ingredientState.cycle().map(craftingState::getResource).orElseGet(() -> {
            this.craftingState = childResult.childTree.craftingState;
            listener.childCalculationCompleted(
                resourceState.resource(),
                childResult.amountCrafted,
                childResult.childTree.listener
            );
            return null;
        });
    }

    private record ChildCalculationResult<T>(boolean success,
                                             long amountCrafted,
                                             CraftingTree<T> childTree) {
    }

    enum CalculationResult {
        SUCCESS,
        MISSING_RESOURCES
    }
}
