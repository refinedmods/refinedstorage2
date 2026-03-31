package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpExecutionPlanStep;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskPlan;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Helper utility for LP (Linear Programming) dispatcher task conversions.
 * Contains static methods for converting LP execution plans to task plans.
 */
public final class LpDispatcherHelper {
    private LpDispatcherHelper() {
    }

    /**
     * Converts an LP crafting solver outcome to a task plan.
     */
    public static Optional<TaskPlan> toTaskPlan(final ResourceKey resource,
                                          final long amount,
                                          final List<LpExecutionPlanStep> steps) {
        final Pattern rootPattern = findRootPatternPrivate(resource, steps);
        if (rootPattern == null) {
            return Optional.empty();
        }

        final Map<Pattern, PatternPlanAccumulator> accumulators = new LinkedHashMap<>();
        for (final LpExecutionPlanStep step : steps) {
            final Pattern pattern = step.recipe().pattern();
            final PatternPlanAccumulator accumulator = accumulators.computeIfAbsent(
                pattern,
                ignored -> new PatternPlanAccumulator(pattern.equals(rootPattern))
            );
            accumulator.addIterations(step.iterations());
            addIngredientUsage(accumulator, pattern, step.iterations());
        }

        final Map<Pattern, TaskPlan.PatternPlan> patterns = new LinkedHashMap<>();
        accumulators.forEach((pattern, accumulator) -> patterns.put(pattern, accumulator.toPlan()));
        return Optional.of(new TaskPlan(
            resource,
            amount,
            rootPattern,
            patterns,
            computeInitialRequirements(steps, rootPattern)
        ));
    }

    /**
     * Creates a single-step task plan from an LP execution step.
     */
    public static TaskPlan toSingleStepPlan(final ResourceKey requestedResource,
                                      final long requestedAmount,
                                      final LpExecutionPlanStep step,
                                      final boolean root) {
        final Pattern pattern = step.recipe().pattern();
        final long iterations = step.iterations();

        final Map<Integer, Map<ResourceKey, Long>> ingredients = new LinkedHashMap<>();
        final List<ResourceAmount> initialRequirements = new ArrayList<>();
        for (int ingredientIndex = 0; ingredientIndex < pattern.layout().ingredients().size(); ingredientIndex++) {
            final var ingredient = pattern.layout().ingredients().get(ingredientIndex);
            final ResourceKey resource = ingredient.inputs().getFirst();
            final long totalAmount = ingredient.amount() * iterations;
            ingredients.put(ingredientIndex, Map.of(resource, totalAmount));
            initialRequirements.add(new ResourceAmount(resource, totalAmount));
        }

        final Map<Pattern, TaskPlan.PatternPlan> patterns = Map.of(
            pattern,
            new TaskPlan.PatternPlan(root, iterations, Map.copyOf(ingredients))
        );

        final ResourceKey outputResource = pattern.layout().outputs().isEmpty()
            ? requestedResource
            : pattern.layout().outputs().getFirst().resource();
        final long outputAmount = pattern.layout().outputs().isEmpty()
            ? iterations
            : pattern.layout().outputs().getFirst().amount() * iterations;
        final long taskAmount = requestedAmount > 0 ? requestedAmount : outputAmount;

        return new TaskPlan(
            outputResource,
            taskAmount,
            pattern,
            patterns,
            List.copyOf(initialRequirements)
        );
    }

    /**
     * Creates a dispatcher task plan.
     */
    public static TaskPlan createDispatcherPlan(final ResourceKey resource,
                                         final long amount,
                                         final Pattern rootPattern) {
        return new TaskPlan(
            resource,
            amount,
            rootPattern,
            Map.of(rootPattern, new TaskPlan.PatternPlan(true, 1, Map.of())),
            List.of()
        );
    }

    private static void addIngredientUsage(final PatternPlanAccumulator accumulator,
                                           final Pattern pattern,
                                           final long iterations) {
        for (int ingredientIndex = 0; ingredientIndex < pattern.layout().ingredients().size(); ingredientIndex++) {
            final var ingredient = pattern.layout().ingredients().get(ingredientIndex);
            final ResourceKey ingredientResource = ingredient.inputs().getFirst();
            accumulator.addIngredient(ingredientIndex, ingredientResource, ingredient.amount() * iterations);
        }
    }

    private static List<ResourceAmount> computeInitialRequirements(final List<LpExecutionPlanStep> steps,
                                                                   final Pattern rootPattern) {
        final Map<ResourceKey, Long> internalStorage = new LinkedHashMap<>();
        final Map<ResourceKey, Long> initialRequirements = new LinkedHashMap<>();

        for (final LpExecutionPlanStep step : steps) {
            final Pattern pattern = step.recipe().pattern();
            for (int iteration = 0; iteration < step.iterations(); iteration++) {
                consumeIterationInputs(pattern, internalStorage, initialRequirements);
                if (!pattern.equals(rootPattern)) {
                    addIterationOutputs(pattern, internalStorage);
                }
            }
        }

        return initialRequirements.entrySet().stream()
            .map(entry -> new ResourceAmount(entry.getKey(), entry.getValue()))
            .toList();
    }

    private static void consumeIterationInputs(final Pattern pattern,
                                               final Map<ResourceKey, Long> internalStorage,
                                               final Map<ResourceKey, Long> initialRequirements) {
        pattern.layout().ingredients().forEach(ingredient -> {
            final ResourceKey resource = ingredient.inputs().getFirst();
            final long amount = ingredient.amount();
            final long available = internalStorage.getOrDefault(resource, 0L);
            final long fromInternalStorage = Math.min(available, amount);
            final long missing = amount - fromInternalStorage;
            if (fromInternalStorage > 0) {
                internalStorage.put(resource, available - fromInternalStorage);
            }
            if (missing > 0) {
                initialRequirements.merge(resource, missing, Long::sum);
            }
        });
    }

    private static void addIterationOutputs(final Pattern pattern,
                                            final Map<ResourceKey, Long> internalStorage) {
        pattern.layout().outputs().forEach(output ->
            internalStorage.merge(output.resource(), output.amount(), Long::sum));
        pattern.layout().byproducts().forEach(byproduct ->
            internalStorage.merge(byproduct.resource(), byproduct.amount(), Long::sum));
    }

    /**
     * Finds the root pattern (final output pattern) in the execution plan.
     */
    public static Pattern findRootPattern(final ResourceKey resource,
                                   final List<LpExecutionPlanStep> steps) {
        for (int index = steps.size() - 1; index >= 0; index--) {
            final Pattern pattern = steps.get(index).recipe().pattern();
            final boolean producesResource = pattern.layout().outputs().stream()
                .anyMatch(output -> output.resource().equals(resource));
            if (producesResource) {
                return pattern;
            }
        }
        return null;
    }

    private static Pattern findRootPatternPrivate(final ResourceKey resource,
                                           final List<LpExecutionPlanStep> steps) {
        for (int index = steps.size() - 1; index >= 0; index--) {
            final Pattern pattern = steps.get(index).recipe().pattern();
            final boolean producesResource = pattern.layout().outputs().stream()
                .anyMatch(output -> output.resource().equals(resource));
            if (producesResource) {
                return pattern;
            }
        }
        return null;
    }

    /**
     * Internal accumulator for building pattern plans from LP steps.
     */
    static final class PatternPlanAccumulator {
        private final boolean root;
        private final Map<Integer, Map<ResourceKey, Long>> ingredients = new LinkedHashMap<>();
        private long iterations;

        PatternPlanAccumulator(final boolean root) {
            this.root = root;
        }

        void addIterations(final long additionalIterations) {
            this.iterations += additionalIterations;
        }

        void addIngredient(final int ingredientIndex,
                          final ResourceKey resource,
                          final long amount) {
            ingredients.computeIfAbsent(ingredientIndex, ignored -> new LinkedHashMap<>())
                .merge(resource, amount, Long::sum);
        }

        TaskPlan.PatternPlan toPlan() {
            final Map<Integer, Map<ResourceKey, Long>> copiedIngredients = new LinkedHashMap<>();
            ingredients.forEach((ingredientIndex, resources) ->
                copiedIngredients.put(ingredientIndex, Map.copyOf(new LinkedHashMap<>(resources))));
            return new TaskPlan.PatternPlan(root, iterations, Map.copyOf(copiedIngredients));
        }
    }
}
