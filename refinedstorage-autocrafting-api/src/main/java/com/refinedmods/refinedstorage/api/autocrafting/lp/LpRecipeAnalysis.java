package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

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
    // Contains functions for things like cycle detection and pruning relevant recipes/resources
    private LpRecipeAnalysis() {
    }

    static Set<ResourceKey> collectRelevantResourceKeys(final List<LpPatternRecipe> recipes) {
        // Given a set of relevant recipes, collects all relevant resource keys
        final Set<ResourceKey> relevantResourceKeys = new LinkedHashSet<>();
        for (final LpPatternRecipe recipe : recipes) {
            relevantResourceKeys.addAll(recipe.output().resourceKeys());
            relevantResourceKeys.addAll(recipe.input().resourceKeys());
        }
        return relevantResourceKeys;
    }

    static List<LpPatternRecipe> prioritizeAndPruneRelevantRecipes(final List<LpPatternRecipe> recipes,
                                                                          final LpResourceSet target) {
        // Gives "relevant" recipes (recipes in the target's crafting tree) a priority
        // Priority is inherited, so if recipe A > recipe B, A's children get priority over B
        // Returns the prioritized recipes, sorted by priority
        Objects.requireNonNull(recipes, "recipes cannot be null");
        Objects.requireNonNull(target, "target cannot be null");
        final TraversalState state = initializeTraversalState(target);
        propagatePriorities(recipes, state);
        return buildPrioritizedRecipes(recipes, state.bestRecipePriorities());
    }

    private static TraversalState initializeTraversalState(final LpResourceSet target) {
        // Reduced cyclotomic complexity does not improve function quality
        final Map<ResourceKey, LpRecipePriorityKey> bestResourcePriorities = new LinkedHashMap<>();
        final Map<UUID, LpRecipePriorityKey> bestRecipePriorities = new LinkedHashMap<>();
        final List<ResourcePriorityEntry> queue = new ArrayList<>();
        final LpRecipePriorityKey basePriority = new LpRecipePriorityKey();
        for (final ResourceKey targetResource : target.resourceKeys()) {
            bestResourcePriorities.put(targetResource, basePriority);
            queue.add(new ResourcePriorityEntry(targetResource, basePriority));
        }
        return new TraversalState(bestResourcePriorities, bestRecipePriorities, queue);
    }

    private static void propagatePriorities(final List<LpPatternRecipe> recipes, final TraversalState state) {
        // Reduced cyclotomic complexity does not improve function quality
        while (!state.queue().isEmpty()) {
            final ResourcePriorityEntry outputEntry = state.queue().removeLast();
            applyOutputPriorityToRecipes(recipes, outputEntry, state);
        }
    }

    private static void applyOutputPriorityToRecipes(final List<LpPatternRecipe> recipes,
                                                     final ResourcePriorityEntry outputEntry,
                                                     final TraversalState state) {
        // Reduced cyclotomic complexity does not improve function quality
        for (final LpPatternRecipe recipe : recipes) {
            if (!recipe.produces(outputEntry.resource())) {
                continue;
            }

            final LpRecipePriorityKey candidateRecipePriority = outputEntry.priority().appendRecipePriority(recipe);
            if (!tryUpdateBestRecipePriority(recipe.uniqueId(), candidateRecipePriority, state.bestRecipePriorities())) {
                continue;
            }
            pushImprovedInputPriorities(recipe, candidateRecipePriority, state);
        }
    }

    private static boolean tryUpdateBestRecipePriority(final UUID recipeId,
                                                       final LpRecipePriorityKey candidatePriority,
                                                       final Map<UUID, LpRecipePriorityKey> bestRecipePriorities) {
        // Reduced cyclotomic complexity does not improve function quality
        final LpRecipePriorityKey currentPriority = bestRecipePriorities.get(recipeId);
        if (currentPriority != null && candidatePriority.compareTo(currentPriority) >= 0) {
            return false;
        }
        bestRecipePriorities.put(recipeId, candidatePriority);
        return true;
    }

    private static void pushImprovedInputPriorities(final LpPatternRecipe recipe,
                                                    final LpRecipePriorityKey candidateRecipePriority,
                                                    final TraversalState state) {
        // Reduced cyclotomic complexity does not improve function quality
        for (final ResourceKey inputResource : recipe.input().resourceKeys()) {
            final LpRecipePriorityKey currentInputPriority = state.bestResourcePriorities().get(inputResource);
            if (currentInputPriority != null && candidateRecipePriority.compareTo(currentInputPriority) >= 0) {
                continue;
            }
            state.bestResourcePriorities().put(inputResource, candidateRecipePriority);
            state.queue().add(new ResourcePriorityEntry(inputResource, candidateRecipePriority));
        }
    }

    private static List<LpPatternRecipe> buildPrioritizedRecipes(
        final List<LpPatternRecipe> recipes,
        final Map<UUID, LpRecipePriorityKey> bestRecipePriorities
    ) {
        // Reduced cyclotomic complexity does not improve function quality
        final List<RecipePriorityEntry> prunedRecipesWithPriority = collectPrunedRecipesWithPriority(
            recipes,
            bestRecipePriorities
        );
        assignEffectivePriorities(prunedRecipesWithPriority, recipes);
        return mapEntriesToRecipes(prunedRecipesWithPriority, recipes);
    }

    private static List<RecipePriorityEntry> collectPrunedRecipesWithPriority(
        final List<LpPatternRecipe> recipes,
        final Map<UUID, LpRecipePriorityKey> bestRecipePriorities
    ) {
        // Reduced cyclotomic complexity does not improve function quality
        final List<RecipePriorityEntry> entries = new ArrayList<>();
        for (final LpPatternRecipe recipe : recipes) {
            final LpRecipePriorityKey priority = bestRecipePriorities.get(recipe.uniqueId());
            if (priority != null) {
                entries.add(new RecipePriorityEntry(recipe.uniqueId(), priority));
            }
        }
        entries.sort(Comparator.comparing(RecipePriorityEntry::priority).thenComparing(RecipePriorityEntry::recipeId));
        return entries;
    }

    private static void assignEffectivePriorities(final List<RecipePriorityEntry> sortedEntries,
                                                  final List<LpPatternRecipe> recipes) {
        // Reduced cyclotomic complexity does not improve function quality
        final Map<UUID, Integer> effectivePriorities = new LinkedHashMap<>();
        for (int index = 0; index < sortedEntries.size(); index++) {
            final RecipePriorityEntry recipeAndPriority = sortedEntries.get(index);
            effectivePriorities.put(recipeAndPriority.recipeId(), sortedEntries.size() - 1 - index);
        }
        for (final LpPatternRecipe recipe : recipes) {
            final Integer effectivePriority = effectivePriorities.get(recipe.uniqueId());
            if (effectivePriority != null) {
                recipe.setEffectivePriority(effectivePriority);
            }
        }
    }

    private static List<LpPatternRecipe> mapEntriesToRecipes(final List<RecipePriorityEntry> sortedEntries,
                                                             final List<LpPatternRecipe> recipes) {
        // Reduced cyclotomic complexity does not improve function quality
        final Map<UUID, LpPatternRecipe> byId = new LinkedHashMap<>();
        for (final LpPatternRecipe recipe : recipes) {
            byId.put(recipe.uniqueId(), recipe);
        }
        final List<LpPatternRecipe> prioritizedRecipes = new ArrayList<>(sortedEntries.size());
        for (final RecipePriorityEntry entry : sortedEntries) {
            prioritizedRecipes.add(byId.get(entry.recipeId()));
        }
        return prioritizedRecipes;
    }

    private record ResourcePriorityEntry(ResourceKey resource, LpRecipePriorityKey priority) {
        // Reduced cyclotomic complexity does not improve function quality
    }

    private record TraversalState(Map<ResourceKey, LpRecipePriorityKey> bestResourcePriorities,
                                  Map<UUID, LpRecipePriorityKey> bestRecipePriorities,
                                  List<ResourcePriorityEntry> queue) {
        // Reduced cyclotomic complexity does not improve function quality
    }

    private record RecipePriorityEntry(UUID recipeId, LpRecipePriorityKey priority) {
        // Reduced cyclotomic complexity does not improve function quality
    }

    static Set<ResourceKey> collectNonProducibleResources(final List<LpPatternRecipe> recipes,
                                                          final Set<ResourceKey> relevantResourceKeys) {
        // Given resources and recipes that can produce them, determines which resources cannot be produced by any recipe
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
        // Given a list of recipes sorted by priority, selects only the highest priority recipe for each output resource
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
        // Detects cycles in the recipe dependency graph
        // Returns a list of cycles (where each cycle is a list of recipes), and a map of recipe ID to whether it's in any cycle
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

    private static boolean recipeOutputsFeedRecipeInputs(final LpPatternRecipe fromRecipe,
                                                         final LpPatternRecipe toRecipe) {
        // Checks if a recipe produces any resource that another recipe consumes, indicating a directed edge in the dependency graph
        for (final ResourceKey outputResource : fromRecipe.output().resourceKeys()) {
            if (toRecipe.consumes(outputResource)) {
                return true;
            }
        }
        return false;
    }

    private static void depthFirstCollectCycles(final int start,
                                                final int current,
                                                final List<List<Integer>> adjacency,
                                                final boolean[] onPath,
                                                final List<Integer> path,
                                                final Set<List<Integer>> seenCycles,
                                                final List<List<Integer>> cycles) {
        // Recursively finds cycles
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
        // To avoid duplicates, we want to represent each cycle in a canonical form. We can rotate the cycle so that the smallest index comes first.
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
        // Compares two cycles of indices lexicographically
        final int sharedSize = Math.min(left.size(), right.size());
        for (int i = 0; i < sharedSize; i++) {
            final int compare = Integer.compare(left.get(i), right.get(i));
            if (compare != 0) {
                return compare;
            }
        }
        return Integer.compare(left.size(), right.size());
    }

    record CycleDetectionResult(Map<UUID, Boolean> inLoopByRecipeId, List<List<LpPatternRecipe>> cycles) {
        CycleDetectionResult {
            inLoopByRecipeId = Map.copyOf(inLoopByRecipeId);
            cycles = List.copyOf(cycles);
        }
    }

    static Set<UUID> collectLoopClosingRecipeIdsOnTargetBranches(final List<LpPatternRecipe> recipes,
                                                                 final LpResourceSet target) {
        // Collects the IDs of recipes that close loops
        // If you deleted all these recipes, there would be no loops in the crafting tree of the target
        final Map<ResourceKey, List<LpPatternRecipe>> outputToRecipes = buildOutputToRecipes(recipes);
        final Set<UUID> loopClosingRecipeIds = new LinkedHashSet<>();
        for (final ResourceKey targetResource : target.resourceKeys()) {
            final Set<ResourceKey> pathResources = new LinkedHashSet<>();
            pathResources.add(targetResource);
            walkLoopClosingRecipes(targetResource, outputToRecipes, pathResources, loopClosingRecipeIds);
        }
        return loopClosingRecipeIds;
    }

    private static Map<ResourceKey, List<LpPatternRecipe>> buildOutputToRecipes(final List<LpPatternRecipe> recipes) {
        // Builds a map from output resources to the recipes that produce them
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
        // Walks through the recipes to find loop-closing recipes
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

    static Set<ResourceKey> collectLoopEntryDeficitResourcesOnTargetBranches(final List<LpPatternRecipe> recipes,
                                                                             final LpResourceSet target) {
        // Finds the set of item types that you'd need to add in order to make loops startable
        // Similar to collectLoopClosingRecipeIdsOnTargetBranches, but collects resources that can start loops instead of recipes that close loops
        final Map<ResourceKey, List<LpPatternRecipe>> outputToRecipes = buildOutputToRecipes(recipes);
        final Set<ResourceKey> loopEntryDeficitResources = new LinkedHashSet<>();
        for (final ResourceKey targetResource : target.resourceKeys()) {
            final Set<ResourceKey> pathResources = new LinkedHashSet<>();
            pathResources.add(targetResource);
            walkLoopEntryDeficits(targetResource, outputToRecipes, pathResources, loopEntryDeficitResources);
        }
        return loopEntryDeficitResources;
    }

    private static void walkLoopEntryDeficits(final ResourceKey resource,
                                              final Map<ResourceKey, List<LpPatternRecipe>> outputToRecipes,
                                              final Set<ResourceKey> pathResources,
                                              final Set<ResourceKey> loopEntryDeficitResources) {
        // Walks through the recipes to find loop entry deficit resources
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
}
