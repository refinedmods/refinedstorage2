package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Planner for LP-based execution.
 * <p>This class provides methods to build execution plans from recipe usage.</p>
 */
public final class LpExecutionPlanner {
    private LpExecutionPlanner() {
    }

    public static Optional<List<LpExecutionPlanStep>> buildExecutablePlanFromRecipeUsage(
        final List<LpPatternRecipe> recipes,
        final Map<UUID, Long> recipeValues,
        final LpResourceSet startingResources
    ) {
        validateInputs(recipes, recipeValues, startingResources);

        final Map<UUID, Long> remainingCounts = new LinkedHashMap<>();
        for (final LpPatternRecipe recipe : recipes) {
            final long rawValue = recipeValues.getOrDefault(recipe.uniqueId(), 0L);
            if (rawValue < 0) {
                throw new IllegalArgumentException(
                    "Negative usage count for recipe: " + recipe.description()
                );
            }
            if (rawValue > 0) {
                remainingCounts.put(recipe.uniqueId(), rawValue);
            }
        }

        final LpRecipeAnalysis.CycleDetectionResult cycleDetectionResult =
            LpRecipeAnalysis.detectRecipeCycles(recipes);
        final Map<UUID, Boolean> inLoopById = cycleDetectionResult.inLoopByRecipeId();

        final LpResourceSet inventory = startingResources.copy();
        final long totalRemaining = remainingCounts.values().stream().mapToLong(Long::longValue).sum();
        final List<LpExecutionPlanStep> plan = new ArrayList<>();

        final boolean success = recursivelyBacksolvePlan(
            recipes,
            inLoopById,
            remainingCounts,
            inventory,
            totalRemaining,
            plan
        );
        return success ? Optional.of(List.copyOf(plan)) : Optional.empty();
    }

    private static boolean recursivelyBacksolvePlan(final List<LpPatternRecipe> recipes,
                                                    final Map<UUID, Boolean> inLoopById,
                                                    final Map<UUID, Long> remainingCounts,
                                                    final LpResourceSet inventory,
                                                    final long totalRemaining,
                                                    final List<LpExecutionPlanStep> plan) {
        if (totalRemaining == 0) {
            return true;
        }

        final List<Candidate> candidates = buildCandidates(recipes, inLoopById, remainingCounts, inventory);

        if (candidates.isEmpty()) {
            return false;
        }

        for (final Candidate candidate : candidates) {
            for (final long batch : buildBatchAttempts(candidate)) {
                if (tryCandidateBatch(
                    recipes,
                    inLoopById,
                    remainingCounts,
                    inventory,
                    totalRemaining,
                    plan,
                    candidate,
                    batch
                )) {
                    return true;
                }
            }
        }

        return false;
    }

    private static List<Candidate> buildCandidates(final List<LpPatternRecipe> recipes,
                                                   final Map<UUID, Boolean> inLoopById,
                                                   final Map<UUID, Long> remainingCounts,
                                                   final LpResourceSet inventory) {
        return recipes.stream()
            .map(recipe -> toCandidate(recipe, remainingCounts, inventory))
            .filter(Objects::nonNull)
            .sorted(Comparator
                .comparing((Candidate candidate) -> inLoopById.getOrDefault(
                    candidate.recipe.uniqueId(),
                    false
                ))
                .reversed()
                .thenComparing(Candidate::maxBatch, Comparator.reverseOrder())
                .thenComparing(Candidate::remaining, Comparator.reverseOrder())
                .thenComparing(candidate -> candidate.recipe.effectivePriority() == null
                    ? Integer.MAX_VALUE
                    : candidate.recipe.effectivePriority())
                .thenComparing(candidate -> candidate.recipe.uniqueId()))
            .toList();
    }

    private static Candidate toCandidate(final LpPatternRecipe recipe,
                                         final Map<UUID, Long> remainingCounts,
                                         final LpResourceSet inventory) {
        final long remaining = remainingCounts.getOrDefault(recipe.uniqueId(), 0L);
        if (remaining <= 0) {
            return null;
        }
        final long maxBatch = Math.min(remaining, computeMaxAffordableBatch(recipe, inventory));
        if (maxBatch <= 0) {
            return null;
        }
        return new Candidate(recipe, remaining, maxBatch);
    }

    private static List<Long> buildBatchAttempts(final Candidate candidate) {
        final List<Long> tryBatches = new ArrayList<>();
        tryBatches.add(candidate.maxBatch);
        if (candidate.maxBatch > 1) {
            tryBatches.add(1L);
        }
        if (candidate.maxBatch > 2) {
            tryBatches.add(candidate.maxBatch / 2L);
        }
        tryBatches.sort(Comparator.reverseOrder());
        return tryBatches.stream().distinct().toList();
    }

    private static boolean tryCandidateBatch(final List<LpPatternRecipe> recipes,
                                             final Map<UUID, Boolean> inLoopById,
                                             final Map<UUID, Long> remainingCounts,
                                             final LpResourceSet inventory,
                                             final long totalRemaining,
                                             final List<LpExecutionPlanStep> plan,
                                             final Candidate candidate,
                                             final long batch) {
        if (batch <= 0 || batch > candidate.remaining) {
            return false;
        }

        applyRecipeBatch(candidate.recipe, batch, inventory);
        remainingCounts.put(candidate.recipe.uniqueId(), candidate.remaining - batch);
        appendOrMergePlanStep(plan, candidate.recipe, batch);

        final boolean success = recursivelyBacksolvePlan(
            recipes,
            inLoopById,
            remainingCounts,
            inventory,
            totalRemaining - batch,
            plan
        );
        if (success) {
            return true;
        }

        removeOrShrinkLastPlanStep(plan, candidate.recipe, batch);
        remainingCounts.put(candidate.recipe.uniqueId(), candidate.remaining);
        rollbackRecipeBatch(candidate.recipe, batch, inventory);
        return false;
    }

    private static long computeMaxAffordableBatch(final LpPatternRecipe recipe, final LpResourceSet inventory) {
        long maxBatch = Long.MAX_VALUE;
        for (final Map.Entry<ResourceKey, Long> entry : recipe.input().asMap().entrySet()) {
            final long inputCount = entry.getValue();
            if (inputCount <= 0) {
                continue;
            }
            final long available = inventory.getAmount(entry.getKey());
            maxBatch = Math.min(maxBatch, available / inputCount);
        }
        return maxBatch;
    }

    private static void applyRecipeBatch(final LpPatternRecipe recipe,
                                         final long batch,
                                         final LpResourceSet inventory) {
        recipe.input().asMap().forEach((resource, inputCount) ->
            inventory.subtractAmount(resource, inputCount * batch));
        recipe.output().asMap().forEach((resource, outputCount) ->
            inventory.addAmount(resource, outputCount * batch));
    }

    private static void rollbackRecipeBatch(final LpPatternRecipe recipe,
                                            final long batch,
                                            final LpResourceSet inventory) {
        recipe.output().asMap().forEach((resource, outputCount) ->
            inventory.subtractAmount(resource, outputCount * batch));
        recipe.input().asMap().forEach((resource, inputCount) ->
            inventory.addAmount(resource, inputCount * batch));
    }

    private static void appendOrMergePlanStep(final List<LpExecutionPlanStep> plan,
                                              final LpPatternRecipe recipe,
                                              final long batch) {
        if (!plan.isEmpty()) {
            final LpExecutionPlanStep last = plan.getLast();
            if (last.recipe().uniqueId().equals(recipe.uniqueId())) {
                plan.set(plan.size() - 1, new LpExecutionPlanStep(recipe, last.iterations() + batch));
                return;
            }
        }
        plan.add(new LpExecutionPlanStep(recipe, batch));
    }

    private static void removeOrShrinkLastPlanStep(final List<LpExecutionPlanStep> plan,
                                                   final LpPatternRecipe recipe,
                                                   final long batch) {
        if (plan.isEmpty()) {
            return;
        }
        final LpExecutionPlanStep last = plan.getLast();
        if (!last.recipe().uniqueId().equals(recipe.uniqueId())) {
            return;
        }
        if (last.iterations() == batch) {
            plan.removeLast();
            return;
        }
        plan.set(plan.size() - 1, new LpExecutionPlanStep(recipe, last.iterations() - batch));
    }

    private static void validateInputs(final List<LpPatternRecipe> recipes,
                                       final Map<UUID, Long> recipeValues,
                                       final LpResourceSet startingResources) {
        Objects.requireNonNull(recipes, "recipes cannot be null");
        Objects.requireNonNull(recipeValues, "recipeValues cannot be null");
        Objects.requireNonNull(startingResources, "startingResources cannot be null");
    }

    private record Candidate(LpPatternRecipe recipe, long remaining, long maxBatch) {
    }
}
