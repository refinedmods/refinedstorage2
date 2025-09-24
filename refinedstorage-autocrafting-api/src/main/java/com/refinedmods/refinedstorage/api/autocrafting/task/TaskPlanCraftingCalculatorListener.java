package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.Amount;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CalculationException;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationException;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculator;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CraftingCalculatorListener;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Optional;

public class TaskPlanCraftingCalculatorListener implements CraftingCalculatorListener<MutableTaskPlan> {
    private MutableTaskPlan task;

    private TaskPlanCraftingCalculatorListener(final MutableTaskPlan task) {
        this.task = task;
    }

    private TaskPlanCraftingCalculatorListener() {
        this(new MutableTaskPlan());
    }

    public static Optional<TaskPlan> calculatePlan(final CraftingCalculator calculator,
                                                   final ResourceKey resource,
                                                   final long amount,
                                                   final CancellationToken cancellationToken) {
        final TaskPlanCraftingCalculatorListener listener = new TaskPlanCraftingCalculatorListener();
        try {
            calculator.calculate(resource, amount, listener, cancellationToken);
        } catch (final CalculationException | CancellationException e) {
            return Optional.empty();
        }
        return listener.task.getPlan();
    }

    @Override
    public CraftingCalculatorListener<MutableTaskPlan> childCalculationStarted(final Pattern childPattern,
                                                                               final ResourceKey resource,
                                                                               final Amount amount) {
        final MutableTaskPlan copy = task.copy(childPattern, resource, amount.getTotal());
        copy.addOrUpdatePattern(childPattern, amount.iterations());
        return new TaskPlanCraftingCalculatorListener(copy);
    }

    @Override
    public void childCalculationCompleted(final CraftingCalculatorListener<MutableTaskPlan> childListener) {
        this.task = childListener.getData();
    }

    @Override
    public void ingredientsExhausted(final ResourceKey resource, final long amount) {
        task.setMissing();
    }

    @Override
    public void ingredientUsed(final Pattern ingredientPattern,
                               final int ingredientIndex,
                               final ResourceKey resource,
                               final long amount) {
        task.addUsedIngredient(ingredientPattern, ingredientIndex, resource, amount);
    }

    @Override
    public void ingredientExtractedFromStorage(final ResourceKey resource, final long amount) {
        task.addToExtract(resource, amount);
    }

    @Override
    public MutableTaskPlan getData() {
        return task;
    }

    @Override
    public boolean requiresFullMissingResourcesCalculation() {
        return false;
    }
}
