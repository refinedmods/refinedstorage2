package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * Solver for LP-based crafting.
 * <p>This class provides methods to solve crafting problems using linear programming.</p>
 */
public final class LpCraftingSolver {
    private static final int TRACE_SAMPLE_LIMIT = 5;
    private static final String TRACE_PREFIX = "[LpCraftingSolver] ";

    private final LpSolverOptions options;

    public LpCraftingSolver() {
        this(LpSolverOptions.defaults());
    }

    public LpCraftingSolver(final LpSolverOptions options) {
        this.options = Objects.requireNonNull(options, "options cannot be null");
        trace(
            "constructor",
            "initialized solver with options=" + options
        );
    }

    private static void trace(final String point, final String details) {
        // intentionally no-op (diagnostic tracing disabled by default)
    }

    private static String summarizeRecipes(final Collection<LpPatternRecipe> recipes) {
        return "count=" + recipes.size();
    }

    private static String summarizeResourceSet(final LpResourceSet resourceSet) {
        return "resources=" + resourceSet.resourceKeys().size()
            + ", totalAmount=" + resourceSet.totalAmount();
    }

    private static String summarizeResourceKeys(final Collection<ResourceKey> resourceKeys) {
        return "count=" + resourceKeys.size();
    }

    private static String summarizeRecipeValues(final Map<UUID, Long> recipeValues) {
        return "count=" + recipeValues.size();
    }

    private static String summarizeResourceValues(final Map<ResourceKey, Long> resourceValues) {
        return "count=" + resourceValues.size();
    }

    private static String summarizeIds(final Collection<UUID> ids) {
        return "count=" + ids.size();
    }

    public PlanningOutcome solve(final List<LpPatternRecipe> recipes,
                                 final LpResourceSet startingResources,
                                 final LpResourceSet target) {
        trace(
            "solve.enter",
            "recipes{" + summarizeRecipes(recipes) + "}, startingResources{"
                + summarizeResourceSet(startingResources) + "}, target{"
                + summarizeResourceSet(target) + "}, options=" + options
        );
        final long maxCraftableAmount = computeMaxCraftableTargetAmount(
            recipes, startingResources, target
        );
        trace("solve.maxCraftable", "maxCraftableAmount=" + maxCraftableAmount);
        if (maxCraftableAmount == 0) {
            trace(
                "solve.noCraftableAmount",
                "target cannot currently be crafted, computing required base items"
            );
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
        trace(
            "solve.cycleEliminationCompleted",
            "hasExecutableResult=" + cycleEliminationResult.executableResult().isPresent()
                + ", fallbackDisabledRecipes{"
                + summarizeIds(cycleEliminationResult.fallbackDisabledRecipeIds()) + "}"
        );
        if (cycleEliminationResult.executableResult().isPresent()) {
            trace(
                "solve.executablePlanFound",
                "planStepCount=" + cycleEliminationResult.executableResult().get().plan().size()
                    + ", recipeValues{"
                    + summarizeRecipeValues(
                        cycleEliminationResult.executableResult().get().solution().recipeValues()
                    ) + "}"
            );
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
        trace(
            "solve.fallbackRequiredBaseItems",
            "reducedRecipes{" + summarizeRecipes(reducedRecipes) + "}, disabledRecipeIds{"
                + summarizeIds(disabledRecipeIds) + "}"
        );
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
        trace(
            "computeMaxCraftableTargetAmount.enter",
            "recipes{" + summarizeRecipes(recipes) + "}, startingResources{"
                + summarizeResourceSet(startingResources) + "}, target{"
                + summarizeResourceSet(target) + "}"
        );
        if (target.isEmpty()) {
            trace("computeMaxCraftableTargetAmount.emptyTarget", "target is empty, returning 0");
            return 0;
        }

        final LpRecipeAnalysis.PrioritizedRecipeSet prioritized =
            LpRecipeAnalysis.prioritizeAndPruneRelevantRecipesAndItems(copyRecipes(recipes), target);
        final Set<ResourceKey> relevantResources = new LinkedHashSet<>(prioritized.relevantResourceKeys());
        relevantResources.addAll(target.resourceKeys());
        trace(
            "computeMaxCraftableTargetAmount.prioritized",
            "prioritizedRecipes{" + summarizeRecipes(prioritized.recipes()) + "}, relevantResources{"
                + summarizeResourceKeys(relevantResources) + "}"
        );

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
            trace(
                "computeMaxCraftableTargetAmount.noFeasibleResult",
                "objectiveResource=" + targetResource + ", returning 0"
            );
            return 0;
        }

        final long maxCraftableAmount = Math.max(
            0L,
            result.finalInventoryValues().getOrDefault(targetResource, 0L)
                - startingResources.getAmount(targetResource)
        );
        trace(
            "computeMaxCraftableTargetAmount.result",
            "objectiveResource=" + targetResource + ", recipeValues{"
                + summarizeRecipeValues(result.recipeValues()) + "}, finalInventoryValues{"
                + summarizeResourceValues(result.finalInventoryValues()) + "}, maxCraftableAmount="
                + maxCraftableAmount
        );
        return maxCraftableAmount;
    }

    public LpResourceSet computeRequiredBaseItems(final List<LpPatternRecipe> recipes,
                                                  final LpResourceSet startingResources,
                                                  final LpResourceSet target) {
        validateInputs(recipes, startingResources, target);
        trace(
            "computeRequiredBaseItems.enter",
            "recipes{" + summarizeRecipes(recipes) + "}, startingResources{"
                + summarizeResourceSet(startingResources) + "}, target{"
                + summarizeResourceSet(target) + "}"
        );

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
        trace(
            "computeRequiredBaseItems.analysis",
            "prioritizedRecipes{" + summarizeRecipes(prioritized.recipes()) + "}, selectedRecipes{"
                + summarizeRecipes(selectedRecipes) + "}, relevantResources{"
                + summarizeResourceKeys(relevantResources) + "}, deficitResources{"
                + summarizeResourceKeys(deficitResources) + "}"
        );

        if (selectedRecipes.isEmpty()) {
            final LpResourceSet required = new LpResourceSet();
            for (final ResourceKey resource : deficitResources) {
                final long needed = Math.max(0L, target.getAmount(resource) - startingResources.getAmount(resource));
                if (needed > 0) {
                    required.addAmount(resource, needed);
                }
            }
            trace(
                "computeRequiredBaseItems.noSelectedRecipes",
                "requiredBaseItems{" + summarizeResourceSet(required) + "}"
            );
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
        trace(
            "computeRequiredBaseItems.lexicographicResult",
            "resultPresent=" + (result != null)
                + (result == null
                ? ""
                : ", recipeValues{" + summarizeRecipeValues(result.recipeValues())
                    + "}, finalInventoryValues{" + summarizeResourceValues(result.finalInventoryValues()) + "}")
        );

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
        trace(
            "computeRequiredBaseItems.result",
            "requiredBaseItems{" + summarizeResourceSet(required) + "}"
        );
        return required;
    }

    public CycleEliminationResult findExecutableSolutionViaCycleElimination(final List<LpPatternRecipe> recipes,
                                                                            final LpResourceSet startingResources,
                                                                            final LpResourceSet target) {
        validateInputs(recipes, startingResources, target);
        trace(
            "findExecutableSolutionViaCycleElimination.enter",
            "recipes{" + summarizeRecipes(recipes) + "}, startingResources{"
                + summarizeResourceSet(startingResources) + "}, target{"
                + summarizeResourceSet(target) + "}, maxBranches="
                + options.maxCycleEliminationBranches()
        );

        final ArrayDeque<Set<UUID>> attempts = new ArrayDeque<>();
        attempts.push(Set.of());

        final Set<List<UUID>> visited = new LinkedHashSet<>();
        visited.add(List.of());

        Set<UUID> bestFallbackDisabledRecipeIds = Set.of();
        int exploredBranches = 0;

        while (!attempts.isEmpty() && exploredBranches < options.maxCycleEliminationBranches()) {
            final Set<UUID> disabledRecipeIds = attempts.pop();
            exploredBranches++;
            trace(
                "findExecutableSolutionViaCycleElimination.branch",
                "exploredBranches=" + exploredBranches + ", remainingAttempts=" + attempts.size()
                    + ", disabledRecipeIds{" + summarizeIds(disabledRecipeIds) + "}"
            );

            final Optional<LpCraftingSolution> solution = solveWithDisabledRecipes(
                recipes,
                startingResources,
                target,
                disabledRecipeIds
            );
            if (solution.isEmpty()) {
                trace(
                    "findExecutableSolutionViaCycleElimination.noSolution",
                    "disabledRecipeIds{" + summarizeIds(disabledRecipeIds) + "}"
                );
                bestFallbackDisabledRecipeIds = keepLargerSet(bestFallbackDisabledRecipeIds, disabledRecipeIds);
                continue;
            }
            trace(
                "findExecutableSolutionViaCycleElimination.solution",
                "recipeValues{" + summarizeRecipeValues(solution.get().recipeValues())
                    + "}, finalInventoryValues{"
                    + summarizeResourceValues(solution.get().finalInventoryValues()) + "}"
            );

            final Optional<List<LpExecutionPlanStep>> plan = LpExecutionPlanner.buildExecutablePlanFromRecipeUsage(
                recipes,
                solution.get().recipeValues(),
                startingResources
            );
            if (plan.isPresent()) {
                trace(
                    "findExecutableSolutionViaCycleElimination.executablePlan",
                    "planStepCount=" + plan.get().size()
                );
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
                trace(
                    "findExecutableSolutionViaCycleElimination.noCyclesInUsedRecipes",
                    "usedRecipes{" + summarizeRecipes(usedRecipes) + "}"
                );
                bestFallbackDisabledRecipeIds = keepLargerSet(bestFallbackDisabledRecipeIds, disabledRecipeIds);
                continue;
            }
            trace(
                "findExecutableSolutionViaCycleElimination.cyclesDetected",
                "cycleCount=" + cycleDetectionResult.cycles().size() + ", usedRecipes{"
                    + summarizeRecipes(usedRecipes) + "}"
            );

            enqueueCycleBreakAttempts(
                cycleDetectionResult.cycles(),
                solution.get(),
                disabledRecipeIds,
                visited,
                attempts
            );
        }

        trace(
            "findExecutableSolutionViaCycleElimination.fallback",
            "exploredBranches=" + exploredBranches + ", fallbackDisabledRecipeIds{"
                + summarizeIds(bestFallbackDisabledRecipeIds) + "}"
        );
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
            trace(
                "enqueueCycleBreakAttempts.cycle",
                "cycleLength=" + cycle.size() + ", cycleRecipes{" + summarizeRecipes(cycle)
                    + "}, currentDisabledRecipeIds{" + summarizeIds(disabledRecipeIds) + "}"
            );
            final Optional<LpPatternRecipe> recipeToDisable = cycle.stream()
                .max(Comparator.comparingLong(solution::recipeUsageCount));
            if (recipeToDisable.isEmpty()) {
                trace("enqueueCycleBreakAttempts.noRecipeSelected", "cycle produced no recipe to disable");
                continue;
            }
            trace(
                "enqueueCycleBreakAttempts.recipeSelected",
                "recipeToDisable=" + recipeToDisable.get().uniqueId()
                    + ", usageCount=" + solution.recipeUsageCount(recipeToDisable.get())
            );
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
            trace(
                "addAttemptIfUnseen.alreadyDisabled",
                "recipeIdToDisable=" + recipeIdToDisable
            );
            return;
        }

        final Set<UUID> nextDisabledRecipeIds = new LinkedHashSet<>(disabledRecipeIds);
        nextDisabledRecipeIds.add(recipeIdToDisable);
        final List<UUID> key = nextDisabledRecipeIds.stream().sorted().toList();
        if (visited.add(key)) {
            attempts.push(Set.copyOf(nextDisabledRecipeIds));
            trace(
                "addAttemptIfUnseen.enqueued",
                "newDisabledRecipeIds{" + summarizeIds(nextDisabledRecipeIds) + "}, attemptsSize="
                    + attempts.size() + ", visitedSize=" + visited.size()
            );
            return;
        }
        trace(
            "addAttemptIfUnseen.duplicate",
            "newDisabledRecipeIds{" + summarizeIds(nextDisabledRecipeIds) + "}, visitedSize="
                + visited.size()
        );
    }

    private Optional<LpCraftingSolution> solveWithDisabledRecipes(final List<LpPatternRecipe> recipes,
                                                                  final LpResourceSet startingResources,
                                                                  final LpResourceSet target,
                                                                  final Set<UUID> disabledRecipeIds) {
        trace(
            "solveWithDisabledRecipes.enter",
            "recipes{" + summarizeRecipes(recipes) + "}, startingResources{"
                + summarizeResourceSet(startingResources) + "}, target{"
                + summarizeResourceSet(target) + "}, disabledRecipeIds{"
                + summarizeIds(disabledRecipeIds) + "}"
        );
        final LpRecipeAnalysis.PrioritizedRecipeSet prioritized =
            LpRecipeAnalysis.prioritizeAndPruneRelevantRecipesAndItems(copyRecipes(recipes), target);
        final Set<ResourceKey> relevantResources = new LinkedHashSet<>(prioritized.relevantResourceKeys());
        relevantResources.addAll(target.resourceKeys());
        trace(
            "solveWithDisabledRecipes.prioritized",
            "prioritizedRecipes{" + summarizeRecipes(prioritized.recipes()) + "}, relevantResources{"
                + summarizeResourceKeys(relevantResources) + "}"
        );

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
            trace("solveWithDisabledRecipes.noResult", "solver returned no feasible result");
            return Optional.empty();
        }

        final List<ResourceKey> sortedRelevantResources = relevantResources.stream()
            .sorted(Comparator.comparing(Object::toString))
            .toList();
        trace(
            "solveWithDisabledRecipes.result",
            "sortedRelevantResources{" + summarizeResourceKeys(sortedRelevantResources)
                + "}, recipeValues{" + summarizeRecipeValues(result.recipeValues())
                + "}, finalInventoryValues{" + summarizeResourceValues(result.finalInventoryValues()) + "}"
        );
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
                        ? Integer.MIN_VALUE
                        : recipe.effectivePriority())
                    .thenComparing(LpPatternRecipe::uniqueId))
                .toList();
            this.relevantResources = Set.copyOf(relevantResources);
            this.startingResources = startingResources.copy();
            this.target = target.copy();
            this.constrainedResources = Set.copyOf(constrainedResources);
            this.disabledRecipeIds = Set.copyOf(disabledRecipeIds);
            this.options = options;
            trace(
                "flowSearchModel.constructor",
                "recipes{" + summarizeRecipes(this.recipes) + "}, relevantResources{"
                    + summarizeResourceKeys(this.relevantResources) + "}, startingResources{"
                    + summarizeResourceSet(this.startingResources) + "}, target{"
                    + summarizeResourceSet(this.target) + "}, constrainedResources{"
                    + summarizeResourceKeys(this.constrainedResources) + "}, disabledRecipeIds{"
                    + summarizeIds(this.disabledRecipeIds) + "}, options=" + this.options
            );
        }

        private FlowSearchResult lexicographicMinimum() {
            trace(
                "flowSearchModel.lexicographicMinimum.enter",
                "reversePriorityRecipes{" + summarizeRecipes(reversePriorityRecipes) + "}"
            );
            final FlowSearchResult feasibilityResult = solveWithObjective(null, null, false, Map.of());
            if (feasibilityResult == null) {
                trace("flowSearchModel.lexicographicMinimum.noFeasibleResult", "initial feasibility solve failed");
                return null;
            }
            trace(
                "flowSearchModel.lexicographicMinimum.feasible",
                "recipeValues{" + summarizeRecipeValues(feasibilityResult.recipeValues())
                    + "}, finalInventoryValues{"
                    + summarizeResourceValues(feasibilityResult.finalInventoryValues()) + "}"
            );

            final Map<UUID, Long> lockedRecipeValues = new LinkedHashMap<>();
            for (final LpPatternRecipe recipe : reversePriorityRecipes) {
                final FlowSearchResult result = solveWithObjective(null, recipe.uniqueId(), false, lockedRecipeValues);
                Objects.requireNonNull(result, "Expected lexicographic lock step to remain feasible");
                lockedRecipeValues.put(recipe.uniqueId(), result.recipeValues().getOrDefault(recipe.uniqueId(), 0L));
                trace(
                    "flowSearchModel.lexicographicMinimum.recipeLocked",
                    "objectiveRecipeId=" + recipe.uniqueId() + ", lockedValue="
                        + lockedRecipeValues.get(recipe.uniqueId()) + ", lockedRecipeValues{"
                        + summarizeRecipeValues(lockedRecipeValues) + "}"
                );
            }
            final FlowSearchResult finalResult = solveWithObjective(null, null, false, lockedRecipeValues);
            trace(
                "flowSearchModel.lexicographicMinimum.finalResult",
                "resultPresent=" + (finalResult != null)
                    + (finalResult == null
                    ? ""
                    : ", recipeValues{" + summarizeRecipeValues(finalResult.recipeValues())
                        + "}, finalInventoryValues{"
                        + summarizeResourceValues(finalResult.finalInventoryValues()) + "}")
            );
            return finalResult;
        }

        private FlowSearchResult maximize(final ResourceKey objectiveResource) {
            trace(
                "flowSearchModel.maximize.enter",
                "objectiveResource=" + objectiveResource
            );
            final FlowSearchResult result = solveWithObjective(
                Objects.requireNonNull(objectiveResource, "objectiveResource cannot be null"),
                null,
                true,
                Map.of()
            );
            trace(
                "flowSearchModel.maximize.result",
                "resultPresent=" + (result != null)
                    + (result == null
                    ? ""
                    : ", recipeValues{" + summarizeRecipeValues(result.recipeValues())
                        + "}, finalInventoryValues{" + summarizeResourceValues(result.finalInventoryValues()) + "}")
            );
            return result;
        }

        private FlowSearchResult solveWithObjective(final ResourceKey objectiveResource,
                                                    final UUID objectiveRecipeId,
                                                    final boolean maximize,
                                                    final Map<UUID, Long> lockedRecipeValues) {
            trace(
                "flowSearchModel.solveWithObjective.enter",
                "objectiveResource=" + objectiveResource + ", objectiveRecipeId=" + objectiveRecipeId
                    + ", maximize=" + maximize + ", recipeCount=" + recipes.size()
                    + ", relevantResourceCount=" + relevantResources.size()
                    + ", constrainedResourceCount=" + constrainedResources.size()
                    + ", disabledRecipeIds{" + summarizeIds(disabledRecipeIds) + "}, lockedRecipeValues{"
                    + summarizeRecipeValues(lockedRecipeValues) + "}"
            );
            final ExpressionsBasedModel model = new ExpressionsBasedModel();
            final Map<UUID, Variable> variableByRecipeId = createRecipeVariables(model);
            configureObjective(model, variableByRecipeId, objectiveResource, objectiveRecipeId);
            addResourceConstraints(model, variableByRecipeId);
            addRecipeLocks(model, variableByRecipeId, lockedRecipeValues);

            final Optimisation.Result result = maximize ? model.maximise() : model.minimise();
            trace(
                "flowSearchModel.solveWithObjective.optimized",
                "state=" + result.getState() + ", value=" + result.getValue()
            );
            if (!result.getState().isFeasible()) {
                trace("flowSearchModel.solveWithObjective.infeasible", "result is not feasible");
                return null;
            }

            final Map<UUID, Long> recipeValues = extractUsedRecipeValues(variableByRecipeId);
            trace(
                "flowSearchModel.solveWithObjective.recipeValuesExtracted",
                "recipeValues{" + summarizeRecipeValues(recipeValues) + "}"
            );

            final Map<ResourceKey, Long> finalInventoryValues = computeFinalInventoryValues(recipeValues);
            trace(
                "flowSearchModel.solveWithObjective.finalInventoryComputed",
                "finalInventoryValues{" + summarizeResourceValues(finalInventoryValues) + "}"
            );

            return new FlowSearchResult(
                Map.copyOf(recipeValues),
                Map.copyOf(finalInventoryValues)
            );
        }

        private Map<UUID, Variable> createRecipeVariables(final ExpressionsBasedModel model) {
            final Map<UUID, Variable> variableByRecipeId = new LinkedHashMap<>();
            for (final LpPatternRecipe recipe : recipes) {
                final boolean disabled = disabledRecipeIds.contains(recipe.uniqueId());
                final Variable variable = model.addVariable(recipe.uniqueId().toString())
                    .integer(true)
                    .lower(0)
                    .upper(disabled ? 0 : options.recipeUpperBound());
                variableByRecipeId.put(recipe.uniqueId(), variable);
            }
            trace(
                "flowSearchModel.solveWithObjective.variablesBuilt",
                "variableCount=" + variableByRecipeId.size() + ", disabledVariableCount="
                    + disabledRecipeIds.size() + ", recipeUpperBound=" + options.recipeUpperBound()
            );
            return variableByRecipeId;
        }

        private void configureObjective(final ExpressionsBasedModel model,
                                        final Map<UUID, Variable> variableByRecipeId,
                                        final ResourceKey objectiveResource,
                                        final UUID objectiveRecipeId) {
            if (objectiveResource == null && objectiveRecipeId == null) {
                return;
            }

            final Expression objective = model.newExpression("objective").weight(1);
            for (final LpPatternRecipe recipe : recipes) {
                final long coefficient = objectiveCoefficient(recipe, objectiveResource, objectiveRecipeId);
                if (coefficient != 0) {
                    objective.set(variableByRecipeId.get(recipe.uniqueId()), coefficient);
                }
            }
            trace(
                "flowSearchModel.solveWithObjective.objectiveBuilt",
                "objectiveResource=" + objectiveResource + ", objectiveRecipeId=" + objectiveRecipeId
            );
        }

        private long objectiveCoefficient(final LpPatternRecipe recipe,
                                          final ResourceKey objectiveResource,
                                          final UUID objectiveRecipeId) {
            if (objectiveRecipeId != null) {
                return recipe.uniqueId().equals(objectiveRecipeId) ? 1 : 0;
            }
            return recipe.coefficient(objectiveResource);
        }

        private void addResourceConstraints(final ExpressionsBasedModel model,
                                            final Map<UUID, Variable> variableByRecipeId) {
            for (final ResourceKey resource : constrainedResources) {
                final Expression expression = model.newExpression("constraint:" + resource);
                final long lowerBound = target.getAmount(resource) - startingResources.getAmount(resource);
                expression.lower(lowerBound);
                for (final LpPatternRecipe recipe : recipes) {
                    final long coefficient = recipe.coefficient(resource);
                    if (coefficient != 0) {
                        expression.set(variableByRecipeId.get(recipe.uniqueId()), coefficient);
                    }
                }
            }
            trace(
                "flowSearchModel.solveWithObjective.constraintsBuilt",
                "constraintCount=" + constrainedResources.size() + ", constrainedResources{"
                    + summarizeResourceKeys(constrainedResources) + "}"
            );
        }

        private void addRecipeLocks(final ExpressionsBasedModel model,
                                    final Map<UUID, Variable> variableByRecipeId,
                                    final Map<UUID, Long> lockedRecipeValues) {
            for (final Map.Entry<UUID, Long> lock : lockedRecipeValues.entrySet()) {
                final Expression lockExpression = model.newExpression("lock:" + lock.getKey());
                lockExpression.level(lock.getValue());
                lockExpression.set(variableByRecipeId.get(lock.getKey()), 1);
            }
            trace(
                "flowSearchModel.solveWithObjective.locksBuilt",
                "lockCount=" + lockedRecipeValues.size() + ", lockedRecipeValues{"
                    + summarizeRecipeValues(lockedRecipeValues) + "}"
            );
        }

        private Map<UUID, Long> extractUsedRecipeValues(final Map<UUID, Variable> variableByRecipeId) {
            final Map<UUID, Long> recipeValues = new LinkedHashMap<>();
            for (final LpPatternRecipe recipe : recipes) {
                final Variable variable = variableByRecipeId.get(recipe.uniqueId());
                final long value = variable.getValue() == null ? 0L : Math.round(variable.getValue().doubleValue());
                if (value > 0) {
                    recipeValues.put(recipe.uniqueId(), value);
                }
            }
            return recipeValues;
        }

        private Map<ResourceKey, Long> computeFinalInventoryValues(final Map<UUID, Long> recipeValues) {
            final Map<ResourceKey, Long> finalInventoryValues = new LinkedHashMap<>();
            for (final ResourceKey resource : relevantResources) {
                long amount = startingResources.getAmount(resource);
                for (final LpPatternRecipe recipe : recipes) {
                    final long usage = recipeValues.getOrDefault(recipe.uniqueId(), 0L);
                    if (usage == 0) {
                        continue;
                    }
                    amount += recipe.coefficient(resource) * usage;
                }
                finalInventoryValues.put(resource, amount);
            }
            return finalInventoryValues;
        }
    }

    private record FlowSearchResult(Map<UUID, Long> recipeValues, Map<ResourceKey, Long> finalInventoryValues) {
    }
}
