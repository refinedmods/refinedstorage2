package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

final class LpRecipeAnalysis {
    private LpRecipeAnalysis() {
    }

    static Set<ResourceKey> collectRelevantResourceKeys(final List<LpPatternRecipe> recipes,
                                                       final LpResourceSet target,
                                                       final Set<ResourceKey> extraResourceKeys) {
        final Set<ResourceKey> relevantResourceKeys = new LinkedHashSet<>();
        relevantResourceKeys.addAll(target.resourceKeys());
        for (final LpPatternRecipe recipe : recipes) {
            relevantResourceKeys.addAll(recipe.output().resourceKeys());
            relevantResourceKeys.addAll(recipe.input().resourceKeys());
        }
        relevantResourceKeys.addAll(extraResourceKeys);
        return relevantResourceKeys;
    }

    static PrioritizedRecipeSet prioritizeAndPruneRelevantRecipesAndItems(final List<LpPatternRecipe> recipes,
                                                                          final LpResourceSet target) {
        Objects.requireNonNull(recipes, "recipes cannot be null");
        Objects.requireNonNull(target, "target cannot be null");

        final Map<ResourceKey, LpRecipePriorityKey> bestResourcePriorities = new LinkedHashMap<>();
        final Map<UUID, LpRecipePriorityKey> bestRecipePriorities = new LinkedHashMap<>();
        final ArrayDeque<ResourceKey> stack = new ArrayDeque<>();

        for (final ResourceKey resource : target.resourceKeys()) {
            bestResourcePriorities.put(resource, new LpRecipePriorityKey());
            stack.push(resource);
        }

        while (!stack.isEmpty()) {
            final ResourceKey outputResource = stack.pop();
            final LpRecipePriorityKey outputPriority = bestResourcePriorities.get(outputResource);
            if (outputPriority == null) {
                continue;
            }

            pushImprovedInputPriorities(
                recipes,
                outputResource,
                outputPriority,
                bestRecipePriorities,
                bestResourcePriorities,
                stack
            );
        }

        final List<Map.Entry<LpPatternRecipe, LpRecipePriorityKey>> prunedRecipes = new ArrayList<>();
        for (final LpPatternRecipe recipe : recipes) {
            final LpRecipePriorityKey priority = bestRecipePriorities.get(recipe.uniqueId());
            if (priority != null) {
                prunedRecipes.add(Map.entry(recipe.copy(), priority));
            }
        }
        prunedRecipes.sort(Map.Entry.comparingByValue());

        final List<LpPatternRecipe> prioritizedRecipes = new ArrayList<>(prunedRecipes.size());
        final Set<ResourceKey> relevantResourceKeys = new LinkedHashSet<>(target.resourceKeys());
        for (int index = 0; index < prunedRecipes.size(); index++) {
            final LpPatternRecipe recipe = prunedRecipes.get(index).getKey();
            recipe.setEffectivePriority(prunedRecipes.size() - 1 - index);
            prioritizedRecipes.add(recipe);
            relevantResourceKeys.addAll(recipe.input().resourceKeys());
            relevantResourceKeys.addAll(recipe.output().resourceKeys());
        }

        return new PrioritizedRecipeSet(prioritizedRecipes, relevantResourceKeys);
    }

    static Set<ResourceKey> collectNonProducibleResources(final List<LpPatternRecipe> recipes,
                                                          final Set<ResourceKey> relevantResourceKeys) {
        final Set<ResourceKey> result = new LinkedHashSet<>();
        for (final ResourceKey resource : relevantResourceKeys) {
            boolean producible = false;
            for (final LpPatternRecipe recipe : recipes) {
                if (recipe.produces(resource)) {
                    producible = true;
                    break;
                }
            }
            if (!producible) {
                result.add(resource);
            }
        }
        return result;
    }

    static List<LpPatternRecipe> selectTopPriorityRecipesPerOutputResource(final List<LpPatternRecipe> recipes) {
        final List<LpPatternRecipe> sorted = recipes.stream()
            .map(LpPatternRecipe::copy)
            .sorted(Comparator
                .comparing((LpPatternRecipe recipe) -> recipe.effectivePriority() == null
                    ? Integer.MIN_VALUE
                    : recipe.effectivePriority())
                .reversed()
                .thenComparing(LpPatternRecipe::uniqueId))
            .toList();

        final Set<ResourceKey> seenOutputResources = new LinkedHashSet<>();
        final Set<UUID> selectedRecipeIds = new LinkedHashSet<>();
        for (final LpPatternRecipe recipe : sorted) {
            boolean producesNewResource = false;
            for (final ResourceKey resource : recipe.output().resourceKeys()) {
                if (!seenOutputResources.contains(resource)) {
                    producesNewResource = true;
                    break;
                }
            }
            if (!producesNewResource) {
                continue;
            }
            selectedRecipeIds.add(recipe.uniqueId());
            seenOutputResources.addAll(recipe.output().resourceKeys());
        }

        final List<LpPatternRecipe> selected = new ArrayList<>();
        for (final LpPatternRecipe recipe : sorted) {
            if (selectedRecipeIds.contains(recipe.uniqueId())) {
                selected.add(recipe);
            }
        }
        return selected;
    }

    static CycleDetectionResult detectRecipeCycles(final List<LpPatternRecipe> recipes) {
        final List<List<Integer>> adjacency = new ArrayList<>(recipes.size());
        for (int fromIndex = 0; fromIndex < recipes.size(); fromIndex++) {
            final LpPatternRecipe fromRecipe = recipes.get(fromIndex);
            final List<Integer> neighbors = new ArrayList<>();
            for (int toIndex = 0; toIndex < recipes.size(); toIndex++) {
                final LpPatternRecipe toRecipe = recipes.get(toIndex);
                if (recipeOutputsFeedRecipeInputs(fromRecipe, toRecipe)) {
                    neighbors.add(toIndex);
                }
            }
            adjacency.add(neighbors);
        }

        final Set<List<Integer>> seenCycles = new HashSet<>();
        final List<List<Integer>> cycleIndices = new ArrayList<>();
        for (int start = 0; start < recipes.size(); start++) {
            final boolean[] onPath = new boolean[recipes.size()];
            onPath[start] = true;
            final List<Integer> path = new ArrayList<>();
            path.add(start);
            depthFirstCollectCycles(start, start, adjacency, onPath, path, seenCycles, cycleIndices);
        }
        cycleIndices.sort(LpRecipeAnalysis::compareIndexCycles);

        final Map<UUID, Boolean> inLoopByRecipeId = new LinkedHashMap<>();
        for (final LpPatternRecipe recipe : recipes) {
            inLoopByRecipeId.put(recipe.uniqueId(), false);
        }

        final List<List<LpPatternRecipe>> cycles = new ArrayList<>();
        for (final List<Integer> cycle : cycleIndices) {
            final List<LpPatternRecipe> resolvedCycle = new ArrayList<>(cycle.size());
            for (final int index : cycle) {
                final LpPatternRecipe recipe = recipes.get(index).copy();
                inLoopByRecipeId.put(recipe.uniqueId(), true);
                resolvedCycle.add(recipe);
            }
            cycles.add(resolvedCycle);
        }

        return new CycleDetectionResult(inLoopByRecipeId, cycles);
    }

    static Set<UUID> collectLoopClosingRecipeIdsOnTargetBranches(final List<LpPatternRecipe> recipes,
                                                                 final LpResourceSet target) {
        final Map<ResourceKey, List<LpPatternRecipe>> outputToRecipes = buildOutputToRecipes(recipes);
        final Set<UUID> loopClosingRecipeIds = new LinkedHashSet<>();
        for (final ResourceKey targetResource : target.resourceKeys()) {
            final Set<ResourceKey> pathResources = new LinkedHashSet<>();
            pathResources.add(targetResource);
            walkLoopClosingRecipes(targetResource, outputToRecipes, pathResources, loopClosingRecipeIds);
        }
        return loopClosingRecipeIds;
    }

    static Set<ResourceKey> collectLoopEntryDeficitResourcesOnTargetBranches(final List<LpPatternRecipe> recipes,
                                                                             final LpResourceSet target) {
        final Map<ResourceKey, List<LpPatternRecipe>> outputToRecipes = buildOutputToRecipes(recipes);
        final Set<ResourceKey> loopEntryDeficitResources = new LinkedHashSet<>();
        for (final ResourceKey targetResource : target.resourceKeys()) {
            final Set<ResourceKey> pathResources = new LinkedHashSet<>();
            pathResources.add(targetResource);
            walkLoopEntryDeficits(targetResource, outputToRecipes, pathResources, loopEntryDeficitResources);
        }
        return loopEntryDeficitResources;
    }

    private static boolean recipeOutputsFeedRecipeInputs(final LpPatternRecipe fromRecipe,
                                                         final LpPatternRecipe toRecipe) {
        for (final ResourceKey outputResource : fromRecipe.output().resourceKeys()) {
            if (toRecipe.consumes(outputResource)) {
                return true;
            }
        }
        return false;
    }

    private static void pushImprovedInputPriorities(final List<LpPatternRecipe> recipes,
                                                    final ResourceKey outputResource,
                                                    final LpRecipePriorityKey outputPriority,
                                                    final Map<UUID, LpRecipePriorityKey> bestRecipePriorities,
                                                    final Map<ResourceKey, LpRecipePriorityKey> bestResourcePriorities,
                                                    final ArrayDeque<ResourceKey> stack) {
        for (final LpPatternRecipe recipe : recipes) {
            if (!recipe.produces(outputResource)) {
                continue;
            }

            final LpRecipePriorityKey candidateRecipePriority = outputPriority.appendRecipePriority(recipe);
            if (!isBetterRecipePriority(recipe, candidateRecipePriority, bestRecipePriorities)) {
                continue;
            }
            bestRecipePriorities.put(recipe.uniqueId(), candidateRecipePriority);
            pushImprovedInputResources(recipe, candidateRecipePriority, bestResourcePriorities, stack);
        }
    }

    private static boolean isBetterRecipePriority(final LpPatternRecipe recipe,
                                                  final LpRecipePriorityKey candidateRecipePriority,
                                                  final Map<UUID, LpRecipePriorityKey> bestRecipePriorities) {
        final LpRecipePriorityKey currentRecipePriority = bestRecipePriorities.get(recipe.uniqueId());
        return currentRecipePriority == null || candidateRecipePriority.compareTo(currentRecipePriority) < 0;
    }

    private static void pushImprovedInputResources(final LpPatternRecipe recipe,
                                                   final LpRecipePriorityKey candidateRecipePriority,
                                                   final Map<ResourceKey, LpRecipePriorityKey> bestResourcePriorities,
                                                   final ArrayDeque<ResourceKey> stack) {
        for (final ResourceKey inputResource : recipe.input().resourceKeys()) {
            final LpRecipePriorityKey currentInputPriority = bestResourcePriorities.get(inputResource);
            if (currentInputPriority == null || candidateRecipePriority.compareTo(currentInputPriority) < 0) {
                bestResourcePriorities.put(inputResource, candidateRecipePriority);
                stack.push(inputResource);
            }
        }
    }

    private static void depthFirstCollectCycles(final int start,
                                                final int current,
                                                final List<List<Integer>> adjacency,
                                                final boolean[] onPath,
                                                final List<Integer> path,
                                                final Set<List<Integer>> seenCycles,
                                                final List<List<Integer>> cycles) {
        for (final int next : adjacency.get(current)) {
            if (next == start && path.size() > 1) {
                final List<Integer> canonical = canonicalizeCycle(path);
                if (seenCycles.add(canonical)) {
                    cycles.add(canonical);
                }
                continue;
            }
            if (onPath[next] || path.size() >= adjacency.size()) {
                continue;
            }
            onPath[next] = true;
            path.add(next);
            depthFirstCollectCycles(start, next, adjacency, onPath, path, seenCycles, cycles);
            path.removeLast();
            onPath[next] = false;
        }
    }

    private static List<Integer> canonicalizeCycle(final List<Integer> cycle) {
        if (cycle.isEmpty()) {
            return List.of();
        }
        List<Integer> best = List.copyOf(cycle);
        for (int shift = 1; shift < cycle.size(); shift++) {
            final List<Integer> rotated = new ArrayList<>(cycle.size());
            rotated.addAll(cycle.subList(shift, cycle.size()));
            rotated.addAll(cycle.subList(0, shift));
            if (compareIndexCycles(rotated, best) < 0) {
                best = List.copyOf(rotated);
            }
        }
        return best;
    }

    private static int compareIndexCycles(final List<Integer> left, final List<Integer> right) {
        final int sharedSize = Math.min(left.size(), right.size());
        for (int i = 0; i < sharedSize; i++) {
            final int compare = Integer.compare(left.get(i), right.get(i));
            if (compare != 0) {
                return compare;
            }
        }
        return Integer.compare(left.size(), right.size());
    }

    private static Map<ResourceKey, List<LpPatternRecipe>> buildOutputToRecipes(final List<LpPatternRecipe> recipes) {
        final Map<ResourceKey, List<LpPatternRecipe>> outputToRecipes = new HashMap<>();
        for (final LpPatternRecipe recipe : recipes) {
            for (final ResourceKey outputResource : recipe.output().resourceKeys()) {
                outputToRecipes.computeIfAbsent(outputResource, ignored -> new ArrayList<>()).add(recipe);
            }
        }
        return outputToRecipes;
    }

    private static void walkLoopClosingRecipes(final ResourceKey resource,
                                               final Map<ResourceKey, List<LpPatternRecipe>> outputToRecipes,
                                               final Set<ResourceKey> pathResources,
                                               final Set<UUID> loopClosingRecipeIds) {
        final List<LpPatternRecipe> producingRecipes = outputToRecipes.get(resource);
        if (producingRecipes == null) {
            return;
        }
        for (final LpPatternRecipe recipe : producingRecipes) {
            for (final ResourceKey inputResource : recipe.input().resourceKeys()) {
                if (pathResources.contains(inputResource)) {
                    loopClosingRecipeIds.add(recipe.uniqueId());
                    continue;
                }
                pathResources.add(inputResource);
                walkLoopClosingRecipes(inputResource, outputToRecipes, pathResources, loopClosingRecipeIds);
                pathResources.remove(inputResource);
            }
        }
    }

    private static void walkLoopEntryDeficits(final ResourceKey resource,
                                              final Map<ResourceKey, List<LpPatternRecipe>> outputToRecipes,
                                              final Set<ResourceKey> pathResources,
                                              final Set<ResourceKey> loopEntryDeficitResources) {
        final List<LpPatternRecipe> producingRecipes = outputToRecipes.get(resource);
        if (producingRecipes == null) {
            return;
        }
        for (final LpPatternRecipe recipe : producingRecipes) {
            for (final ResourceKey inputResource : recipe.input().resourceKeys()) {
                if (pathResources.contains(inputResource)) {
                    loopEntryDeficitResources.add(resource);
                    continue;
                }
                pathResources.add(inputResource);
                walkLoopEntryDeficits(inputResource, outputToRecipes, pathResources, loopEntryDeficitResources);
                pathResources.remove(inputResource);
            }
        }
    }

    record PrioritizedRecipeSet(List<LpPatternRecipe> recipes, Set<ResourceKey> relevantResourceKeys) {
        PrioritizedRecipeSet {
            recipes = List.copyOf(recipes);
            relevantResourceKeys = Set.copyOf(relevantResourceKeys);
        }
    }

    record CycleDetectionResult(Map<UUID, Boolean> inLoopByRecipeId, List<List<LpPatternRecipe>> cycles) {
        CycleDetectionResult {
            inLoopByRecipeId = Map.copyOf(inLoopByRecipeId);
            cycles = List.copyOf(cycles);
        }
    }
}
