package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.Amount;
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
                                                   final long amount) {
        final TaskPlanCraftingCalculatorListener listener = new TaskPlanCraftingCalculatorListener();
        calculator.calculate(resource, amount, listener);
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
    public void childCalculationCancelled(final CraftingCalculatorListener<MutableTaskPlan> childListener) {
        task.clear();
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
}
