package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Solver for LP-based crafting.
 * <p>This class provides methods to solve crafting problems using linear programming.</p>
 */
public final class LpCraftingSolver {
    private final LpSolverOptions options;

    public LpCraftingSolver() {
        this(LpSolverOptions.defaults());
    }

    public LpCraftingSolver(final LpSolverOptions options) {
        this.options = Objects.requireNonNull(options, "options cannot be null");
    }

    public PlanningOutcome solve(final List<LpPatternRecipe> recipes,
                                 final LpResourceSet startingResources,
                                 final LpResourceSet target) {
        final long maxCraftableAmount = computeMaxCraftableTargetAmount(
            recipes, startingResources, target
        );
        if (maxCraftableAmount == 0) {
            return new PlanningOutcome(
                maxCraftableAmount,
                Optional.empty(),
                computeRequiredBaseItems(recipes, startingResources, target),
                Set.of()
            );
        }

        final CycleEliminationResult cycleEliminationResult = findExecutableSolutionViaCycleElimination(
            recipes,
            startingResources,
            target
        );
        if (cycleEliminationResult.executableResult().isPresent()) {
            return new PlanningOutcome(
                maxCraftableAmount,
                cycleEliminationResult.executableResult(),
                LpResourceSet.empty(),
                Set.of()
            );
        }

        final Set<UUID> disabledRecipeIds = cycleEliminationResult.fallbackDisabledRecipeIds();
        final List<LpPatternRecipe> reducedRecipes = recipes.stream()
            .filter(recipe -> !disabledRecipeIds.contains(recipe.uniqueId()))
            .map(LpPatternRecipe::copy)
            .toList();
        return new PlanningOutcome(
            maxCraftableAmount,
            Optional.empty(),
            computeRequiredBaseItems(reducedRecipes, startingResources, target),
            disabledRecipeIds
        );
    }

    private long computeMaxCraftableTargetAmount(
        final List<LpPatternRecipe> recipes,
        final LpResourceSet startingResources,
        final LpResourceSet target
    ) {
        validateInputs(recipes, startingResources, target);
        if (target.isEmpty()) {
            return 0;
        }

        final LpRecipeAnalysis.PrioritizedRecipeSet prioritized =
            LpRecipeAnalysis.prioritizeAndPruneRelevantRecipesAndItems(copyRecipes(recipes), target);
        final Set<ResourceKey> relevantResources = new LinkedHashSet<>(prioritized.relevantResourceKeys());
        relevantResources.addAll(target.resourceKeys());

        final ResourceKey targetResource = target.resourceKeys().iterator().next();
        final FlowSearchResult result = new FlowSearchModel(
            prioritized.recipes(),
            relevantResources,
            startingResources,
            target,
            relevantResources,
            Set.of(),
            options
        ).maximize(targetResource);
        if (result == null) {
            return 0;
        }

        return Math.max(
            0L,
            result.finalInventoryValues().getOrDefault(targetResource, 0L)
                - startingResources.getAmount(targetResource)
        );
    }

    public LpResourceSet computeRequiredBaseItems(final List<LpPatternRecipe> recipes,
                                                  final LpResourceSet startingResources,
                                                  final LpResourceSet target) {
        validateInputs(recipes, startingResources, target);

        final LpRecipeAnalysis.PrioritizedRecipeSet prioritized =
            LpRecipeAnalysis.prioritizeAndPruneRelevantRecipesAndItems(copyRecipes(recipes), target);
        final List<LpPatternRecipe> selectedRecipes =
            LpRecipeAnalysis.selectTopPriorityRecipesPerOutputResource(prioritized.recipes());
        final Set<ResourceKey> relevantResources = LpRecipeAnalysis.collectRelevantResourceKeys(
            selectedRecipes,
            target,
            prioritized.relevantResourceKeys()
        );

        final Set<ResourceKey> deficitResources = new LinkedHashSet<>(
            LpRecipeAnalysis.collectNonProducibleResources(selectedRecipes, relevantResources)
        );
        deficitResources.addAll(
            LpRecipeAnalysis.collectLoopEntryDeficitResourcesOnTargetBranches(selectedRecipes, target)
        );

        if (deficitResources.isEmpty()) {
            final Set<UUID> loopClosingRecipeIds =
                LpRecipeAnalysis.collectLoopClosingRecipeIdsOnTargetBranches(selectedRecipes, target);
            if (!loopClosingRecipeIds.isEmpty()) {
                final List<LpPatternRecipe> reducedRecipes = selectedRecipes.stream()
                    .filter(recipe -> !loopClosingRecipeIds.contains(recipe.uniqueId()))
                    .map(LpPatternRecipe::copy)
                    .toList();
                return computeRequiredBaseItems(reducedRecipes, startingResources, target);
            }
        }

        if (selectedRecipes.isEmpty()) {
            final LpResourceSet required = new LpResourceSet();
            for (final ResourceKey resource : deficitResources) {
                final long needed = Math.max(0L, target.getAmount(resource) - startingResources.getAmount(resource));
                if (needed > 0) {
                    required.addAmount(resource, needed);
                }
            }
            return required;
        }

        final Set<ResourceKey> constrainedResources = new LinkedHashSet<>(relevantResources);
        constrainedResources.removeAll(deficitResources);
        constrainedResources.addAll(
            target.resourceKeys().stream()
                .filter(resource -> !deficitResources.contains(resource))
                .toList()
        );

        final FlowSearchResult result = new FlowSearchModel(
            selectedRecipes,
            relevantResources,
            startingResources,
            target,
            constrainedResources,
            Set.of(),
            options
        ).lexicographicMinimum();

        final Map<ResourceKey, Long> finalInventoryValues =
            result == null ? startingResources.asMap() : result.finalInventoryValues();
        final LpResourceSet required = new LpResourceSet();
        for (final ResourceKey resource : deficitResources) {
            final long finalInventory = finalInventoryValues.getOrDefault(
                resource,
                startingResources.getAmount(resource)
            );
            final long needed = Math.max(0L, target.getAmount(resource) - finalInventory);
            if (needed > 0) {
                required.addAmount(resource, needed);
            }
        }
        return required;
    }

    public CycleEliminationResult findExecutableSolutionViaCycleElimination(final List<LpPatternRecipe> recipes,
                                                                            final LpResourceSet startingResources,
                                                                            final LpResourceSet target) {
        validateInputs(recipes, startingResources, target);

        final ArrayDeque<Set<UUID>> attempts = new ArrayDeque<>();
        attempts.push(Set.of());

        final Set<List<UUID>> visited = new LinkedHashSet<>();
        visited.add(List.of());

        Set<UUID> bestFallbackDisabledRecipeIds = Set.of();
        int exploredBranches = 0;

        while (!attempts.isEmpty() && exploredBranches < options.maxCycleEliminationBranches()) {
            final Set<UUID> disabledRecipeIds = attempts.pop();
            exploredBranches++;

            final Optional<LpCraftingSolution> solution = solveWithDisabledRecipes(
                recipes,
                startingResources,
                target,
                disabledRecipeIds
            );
            if (solution.isEmpty()) {
                bestFallbackDisabledRecipeIds = keepLargerSet(bestFallbackDisabledRecipeIds, disabledRecipeIds);
                continue;
            }

            final Optional<List<LpExecutionPlanStep>> plan = LpExecutionPlanner.buildExecutablePlanFromRecipeUsage(
                recipes,
                solution.get().recipeValues(),
                startingResources
            );
            if (plan.isPresent()) {
                return new CycleEliminationResult(
                    Optional.of(new ExecutablePlanResult(solution.get(), plan.get())),
                    Set.of()
                );
            }

            final List<LpPatternRecipe> usedRecipes = recipes.stream()
                .filter(recipe -> solution.get().recipeUsageCount(recipe) > 0)
                .map(LpPatternRecipe::copy)
                .toList();
            final LpRecipeAnalysis.CycleDetectionResult cycleDetectionResult =
                LpRecipeAnalysis.detectRecipeCycles(usedRecipes);
            if (cycleDetectionResult.cycles().isEmpty()) {
                bestFallbackDisabledRecipeIds = keepLargerSet(bestFallbackDisabledRecipeIds, disabledRecipeIds);
                continue;
            }

            enqueueCycleBreakAttempts(
                cycleDetectionResult.cycles(),
                solution.get(),
                disabledRecipeIds,
                visited,
                attempts
            );
        }

        return new CycleEliminationResult(Optional.empty(), Set.copyOf(bestFallbackDisabledRecipeIds));
    }

    private static Set<UUID> keepLargerSet(final Set<UUID> currentBest,
                                           final Set<UUID> candidate) {
        if (candidate.size() > currentBest.size()) {
            return Set.copyOf(candidate);
        }
        return currentBest;
    }

    private static void enqueueCycleBreakAttempts(final List<List<LpPatternRecipe>> cycles,
                                                  final LpCraftingSolution solution,
                                                  final Set<UUID> disabledRecipeIds,
                                                  final Set<List<UUID>> visited,
                                                  final ArrayDeque<Set<UUID>> attempts) {
        for (final List<LpPatternRecipe> cycle : cycles) {
            final Optional<LpPatternRecipe> recipeToDisable = cycle.stream()
                .max(Comparator.comparingLong(solution::recipeUsageCount));
            if (recipeToDisable.isEmpty()) {
                continue;
            }
            addAttemptIfUnseen(
                disabledRecipeIds,
                recipeToDisable.get().uniqueId(),
                visited,
                attempts
            );
        }
    }

    private static void addAttemptIfUnseen(final Set<UUID> disabledRecipeIds,
                                           final UUID recipeIdToDisable,
                                           final Set<List<UUID>> visited,
                                           final ArrayDeque<Set<UUID>> attempts) {
        if (disabledRecipeIds.contains(recipeIdToDisable)) {
            return;
        }

        final Set<UUID> nextDisabledRecipeIds = new LinkedHashSet<>(disabledRecipeIds);
        nextDisabledRecipeIds.add(recipeIdToDisable);
        final List<UUID> key = nextDisabledRecipeIds.stream().sorted().toList();
        if (visited.add(key)) {
            attempts.push(Set.copyOf(nextDisabledRecipeIds));
        }
    }

    private Optional<LpCraftingSolution> solveWithDisabledRecipes(final List<LpPatternRecipe> recipes,
                                                                  final LpResourceSet startingResources,
                                                                  final LpResourceSet target,
                                                                  final Set<UUID> disabledRecipeIds) {
        final LpRecipeAnalysis.PrioritizedRecipeSet prioritized =
            LpRecipeAnalysis.prioritizeAndPruneRelevantRecipesAndItems(copyRecipes(recipes), target);
        final Set<ResourceKey> relevantResources = new LinkedHashSet<>(prioritized.relevantResourceKeys());
        relevantResources.addAll(target.resourceKeys());

        final FlowSearchResult result = new FlowSearchModel(
            prioritized.recipes(),
            relevantResources,
            startingResources,
            target,
            relevantResources,
            disabledRecipeIds,
            options
        ).lexicographicMinimum();
        if (result == null) {
            return Optional.empty();
        }

        final List<ResourceKey> sortedRelevantResources = relevantResources.stream()
            .sorted(Comparator.comparing(Object::toString))
            .toList();
        return Optional.of(new LpCraftingSolution(
            result.recipeValues(),
            result.finalInventoryValues(),
            sortedRelevantResources
        ));
    }

    private static List<LpPatternRecipe> copyRecipes(final Collection<LpPatternRecipe> recipes) {
        return recipes.stream().map(LpPatternRecipe::copy).toList();
    }

    private static void validateInputs(final List<LpPatternRecipe> recipes,
                                       final LpResourceSet startingResources,
                                       final LpResourceSet target) {
        Objects.requireNonNull(recipes, "recipes cannot be null");
        Objects.requireNonNull(startingResources, "startingResources cannot be null");
        Objects.requireNonNull(target, "target cannot be null");
    }

    public record ExecutablePlanResult(LpCraftingSolution solution, List<LpExecutionPlanStep> plan) {
        public ExecutablePlanResult {
            Objects.requireNonNull(solution, "solution cannot be null");
            plan = List.copyOf(plan);
        }
    }

    public record CycleEliminationResult(Optional<ExecutablePlanResult> executableResult,
                                         Set<UUID> fallbackDisabledRecipeIds) {
        public CycleEliminationResult {
            Objects.requireNonNull(executableResult, "executableResult cannot be null");
            Objects.requireNonNull(fallbackDisabledRecipeIds, "fallbackDisabledRecipeIds cannot be null");
            fallbackDisabledRecipeIds = Set.copyOf(fallbackDisabledRecipeIds);
        }
    }

    public record PlanningOutcome(long maxCraftableAmount,
                                  Optional<ExecutablePlanResult> executableResult,
                                  LpResourceSet requiredBaseItems,
                                  Set<UUID> fallbackDisabledRecipeIds) {
        public PlanningOutcome {
            Objects.requireNonNull(executableResult, "executableResult cannot be null");
            Objects.requireNonNull(requiredBaseItems, "requiredBaseItems cannot be null");
            Objects.requireNonNull(fallbackDisabledRecipeIds, "fallbackDisabledRecipeIds cannot be null");
            fallbackDisabledRecipeIds = Set.copyOf(fallbackDisabledRecipeIds);
        }
    }

    private static final class FlowSearchModel {
        private final List<LpPatternRecipe> recipes;
        private final List<LpPatternRecipe> reversePriorityRecipes;
        private final Set<ResourceKey> relevantResources;
        private final LpResourceSet startingResources;
        private final LpResourceSet target;
        private final Set<ResourceKey> constrainedResources;
        private final Set<UUID> disabledRecipeIds;
        private final LpSolverOptions options;
        private final Map<UUID, Integer> upperBounds;

        private FlowSearchModel(final List<LpPatternRecipe> recipes,
                                final Set<ResourceKey> relevantResources,
                                final LpResourceSet startingResources,
                                final LpResourceSet target,
                                final Set<ResourceKey> constrainedResources,
                                final Set<UUID> disabledRecipeIds,
                                final LpSolverOptions options) {
            this.recipes = List.copyOf(recipes);
            this.reversePriorityRecipes = recipes.stream()
                .sorted(Comparator
                    .comparing((LpPatternRecipe recipe) -> recipe.effectivePriority() == null
                        ? Integer.MAX_VALUE
                        : recipe.effectivePriority())
                    .reversed()
                    .thenComparing(LpPatternRecipe::uniqueId))
                .toList();
            this.relevantResources = Set.copyOf(relevantResources);
            this.startingResources = startingResources.copy();
            this.target = target.copy();
            this.constrainedResources = Set.copyOf(constrainedResources);
            this.disabledRecipeIds = Set.copyOf(disabledRecipeIds);
            this.options = options;
            this.upperBounds = computeUpperBounds();
        }

        private FlowSearchResult lexicographicMinimum() {
            final SearchBudget budget = new SearchBudget(options.maxSearchNodes());
            final Map<UUID, Long> assignments = new LinkedHashMap<>();
            final LpResourceSet currentInventory = startingResources.copy();
            final boolean found = searchLexicographic(0, assignments, currentInventory, budget);
            if (!found) {
                return null;
            }
            return new FlowSearchResult(assignments, currentInventory.asMap());
        }

        private FlowSearchResult maximize(final ResourceKey objectiveResource) {
            final SearchBudget budget = new SearchBudget(options.maxSearchNodes());
            final Map<UUID, Long> assignments = new LinkedHashMap<>();
            final LpResourceSet currentInventory = startingResources.copy();
            final BestResult bestResult = new BestResult(Long.MIN_VALUE, Map.of(), Map.of());
            searchMaximize(0, assignments, currentInventory, objectiveResource, budget, bestResult);
            if (bestResult.bestValue == Long.MIN_VALUE) {
                return null;
            }
            return new FlowSearchResult(bestResult.recipeValues, bestResult.finalInventoryValues);
        }

        private boolean searchLexicographic(final int index,
                                            final Map<UUID, Long> assignments,
                                            final LpResourceSet currentInventory,
                                            final SearchBudget budget) {
            if (!budget.tryAdvance()
                || !canStillReachTargets(index, currentInventory, reversePriorityRecipes)) {
                return false;
            }
            if (index >= reversePriorityRecipes.size()) {
                return constraintsSatisfied(currentInventory);
            }

            final LpPatternRecipe recipe = reversePriorityRecipes.get(index);
            final int upperBound = upperBounds.getOrDefault(recipe.uniqueId(), 0);
            for (int value = 0; value <= upperBound; value++) {
                applyRecipeUsage(recipe, value, currentInventory);
                assignments.put(recipe.uniqueId(), (long) value);
                if (searchLexicographic(index + 1, assignments, currentInventory, budget)) {
                    return true;
                }
                assignments.remove(recipe.uniqueId());
                applyRecipeUsage(recipe, -value, currentInventory);
            }
            return false;
        }

        private void searchMaximize(final int index,
                                    final Map<UUID, Long> assignments,
                                    final LpResourceSet currentInventory,
                                    final ResourceKey objectiveResource,
                                    final SearchBudget budget,
                                    final BestResult bestResult) {
            if (!budget.tryAdvance() || !canStillReachTargets(index, currentInventory, recipes)) {
                return;
            }
            final long optimisticValue = optimisticObjective(index, currentInventory, recipes, objectiveResource);
            if (optimisticValue < bestResult.bestValue) {
                return;
            }
            if (index >= recipes.size()) {
                updateBestLeafResult(
                    assignments,
                    currentInventory,
                    objectiveResource,
                    bestResult
                );
                return;
            }

            final LpPatternRecipe recipe = recipes.get(index);
            final int upperBound = upperBounds.getOrDefault(recipe.uniqueId(), 0);
            for (int value = upperBound; value >= 0; value--) {
                applyRecipeUsage(recipe, value, currentInventory);
                assignments.put(recipe.uniqueId(), (long) value);
                searchMaximize(index + 1, assignments, currentInventory, objectiveResource, budget, bestResult);
                assignments.remove(recipe.uniqueId());
                applyRecipeUsage(recipe, -value, currentInventory);
            }
        }

        private void updateBestLeafResult(final Map<UUID, Long> assignments,
                                          final LpResourceSet currentInventory,
                                          final ResourceKey objectiveResource,
                                          final BestResult bestResult) {
            if (!constraintsSatisfied(currentInventory)) {
                return;
            }

            final long objectiveValue = currentInventory.getAmount(objectiveResource);
            if (objectiveValue > bestResult.bestValue) {
                bestResult.bestValue = objectiveValue;
                bestResult.recipeValues = Map.copyOf(new LinkedHashMap<>(assignments));
                bestResult.finalInventoryValues = Map.copyOf(
                    new LinkedHashMap<>(currentInventory.asMap())
                );
            }
        }

        private Map<UUID, Integer> computeUpperBounds() {
            final Map<UUID, Integer> result = new HashMap<>();
            final long dynamicBase = Math.max(1L, Math.max(startingResources.totalAmount(), target.totalAmount()));
            final int dynamicBound = (int) Math.min(options.recipeUpperBound(), Math.max(8L, dynamicBase * 2L));
            for (final LpPatternRecipe recipe : recipes) {
                result.put(recipe.uniqueId(), disabledRecipeIds.contains(recipe.uniqueId()) ? 0 : dynamicBound);
            }
            return result;
        }

        private boolean canStillReachTargets(final int index,
                                             final LpResourceSet currentInventory,
                                             final List<LpPatternRecipe> orderedRecipes) {
            for (final ResourceKey resource : constrainedResources) {
                long optimistic = currentInventory.getAmount(resource);
                for (int remainingIndex = index; remainingIndex < orderedRecipes.size(); remainingIndex++) {
                    final LpPatternRecipe remainingRecipe = orderedRecipes.get(remainingIndex);
                    final long coefficient = remainingRecipe.coefficient(resource);
                    if (coefficient > 0) {
                        optimistic += coefficient * upperBounds.getOrDefault(remainingRecipe.uniqueId(), 0);
                    }
                }
                if (optimistic < target.getAmount(resource)) {
                    return false;
                }
            }
            return true;
        }

        private long optimisticObjective(final int index,
                                         final LpResourceSet currentInventory,
                                         final List<LpPatternRecipe> orderedRecipes,
                                         final ResourceKey objectiveResource) {
            long optimistic = currentInventory.getAmount(objectiveResource);
            for (int remainingIndex = index; remainingIndex < orderedRecipes.size(); remainingIndex++) {
                final LpPatternRecipe remainingRecipe = orderedRecipes.get(remainingIndex);
                final long coefficient = remainingRecipe.coefficient(objectiveResource);
                if (coefficient > 0) {
                    optimistic += coefficient * upperBounds.getOrDefault(remainingRecipe.uniqueId(), 0);
                }
            }
            return optimistic;
        }

        private boolean constraintsSatisfied(final LpResourceSet currentInventory) {
            for (final ResourceKey resource : constrainedResources) {
                if (currentInventory.getAmount(resource) < target.getAmount(resource)) {
                    return false;
                }
            }
            return true;
        }

        private void applyRecipeUsage(final LpPatternRecipe recipe,
                                      final long multiplier,
                                      final LpResourceSet currentInventory) {
            if (multiplier == 0L) {
                return;
            }
            recipe.output().asMap().forEach((resource, outputCount) ->
                currentInventory.addAmount(resource, outputCount * multiplier));
            recipe.input().asMap().forEach((resource, inputCount) ->
                currentInventory.subtractAmount(resource, inputCount * multiplier));
            for (final ResourceKey resource : relevantResources) {
                if (currentInventory.getAmount(resource) == 0L) {
                    currentInventory.setAmount(resource, 0L);
                }
            }
        }
    }

    private static final class BestResult {
        private long bestValue;
        private Map<UUID, Long> recipeValues;
        private Map<ResourceKey, Long> finalInventoryValues;

        private BestResult(final long bestValue,
                           final Map<UUID, Long> recipeValues,
                           final Map<ResourceKey, Long> finalInventoryValues) {
            this.bestValue = bestValue;
            this.recipeValues = recipeValues;
            this.finalInventoryValues = finalInventoryValues;
        }
    }

    private record FlowSearchResult(Map<UUID, Long> recipeValues, Map<ResourceKey, Long> finalInventoryValues) {
    }

    private static final class SearchBudget {
        private final int maxNodes;
        private int exploredNodes;

        private SearchBudget(final int maxNodes) {
            this.maxNodes = maxNodes;
        }

        private boolean tryAdvance() {
            exploredNodes++;
            return exploredNodes <= maxNodes;
        }
    }
}
