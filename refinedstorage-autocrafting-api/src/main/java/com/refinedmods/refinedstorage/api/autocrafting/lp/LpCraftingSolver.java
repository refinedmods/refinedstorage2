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

public final class LpCraftingSolver {
    // Solves a crafting problem defined by a set of recipes, starting inventory, and target inventory, using an LP solver. 
    // Returns a list of recipes and how many times to use them
    // Might create time travel solutions, such as "oh you want to make a netherite template? Just borrow one from the future, double it, then send one to the past"
    // These can only happen in recipes with cycles, which is what the execution planner prevents

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
        // Main entry point for solving a crafting problem. First computes the max craftable amount of the target, 
        // then tries to find an executable plan via cycle elimination, and if that fails, 
        // computes what crafting tree leaf resources are missing to make it craftable
        // (It does this by eliminating cycles until the tree has leaves)
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
        // Computes the maximum craftable amount of the target resource given the recipes and starting inventory, without regard to execution feasibility.
        // May overestimate in some cases with cycles
        validateInputs(recipes, startingResources, target);
        if (target.isEmpty()) {
            return 0;
        }

        final List<LpPatternRecipe> prioritizedRecipes =
            LpRecipeAnalysis.prioritizeAndPruneRelevantRecipes(copyRecipes(recipes), target);
        final Set<ResourceKey> relevantResources = new LinkedHashSet<>(
            LpRecipeAnalysis.collectRelevantResourceKeys(prioritizedRecipes)
        );
        relevantResources.addAll(target.resourceKeys());

        final ResourceKey targetResource = target.resourceKeys().iterator().next();
        final FlowSearchResult result = new FlowSearchModel(
            prioritizedRecipes,
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

        final long maxCraftableAmount = Math.max(
            0L,
            result.finalInventoryValues().getAmount(targetResource)
                - startingResources.getAmount(targetResource)
        );
        return maxCraftableAmount;
    }

    public LpResourceSet computeRequiredBaseItems(final List<LpPatternRecipe> recipes,
                                                  final LpResourceSet startingResources,
                                                  final LpResourceSet target) {
        // Computes what items are missing in order to craft the target
        // Only looks at crafting tree leaf resources, which are either non-producible or part of cycles
        // Guaranteed to always give a resource set that, when added, will make it craftable
        // Might not give the smallest set in cases where a more resource-efficient recipe has a lower priority than a less efficient one that produces the same resource
        // Might not always give the smallest set when cycles are involved
        validateInputs(recipes, startingResources, target);

        final List<LpPatternRecipe> prioritizedRecipes =
            LpRecipeAnalysis.prioritizeAndPruneRelevantRecipes(copyRecipes(recipes), target);
        final List<LpPatternRecipe> selectedRecipes =
            LpRecipeAnalysis.selectTopPriorityRecipesPerOutputResource(prioritizedRecipes);
        final Set<ResourceKey> relevantResources = 
            LpRecipeAnalysis.collectRelevantResourceKeys(selectedRecipes);
        relevantResources.addAll(target.resourceKeys());

        final Set<ResourceKey> deficitResources = new LinkedHashSet<>(
            LpRecipeAnalysis.collectNonProducibleResources(selectedRecipes, relevantResources)
        );
        deficitResources.addAll(
            LpRecipeAnalysis.collectLoopEntryDeficitResourcesOnTargetBranches(selectedRecipes, target)
        );

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

        final LpResourceSet required = new LpResourceSet();
        for (final ResourceKey resource : deficitResources) {
            long finalInventory;
            if (result == null) {
                finalInventory = startingResources.getAmount(resource);
            } else {
                finalInventory = result.finalInventoryValues().getAmount(resource);
            }
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
        // Uses LP solving to find a solution, then tries to build an execution plan for it.
        // If the solution is unplannable, it starts disabling cycles in the recipe graph until 
        // it finds a plannable solution which it returns
        // or it runs out of cycles to eliminate where it then gets defecit resources
        // or it exhausts its branching limit, where it returns the best solution it found during the search as a fallback, along with the recipes that would need to be disabled to achieve it
        // which is then used to calculate missing resources
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
        // For each cycle, identifies the recipe with the largest usage count in the solution
        // and creates a new attempt with that recipe disabled, if it hasn't been attempted before.
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
        // Checks if disabling the given recipe ID has already been attempted. 
        // If not, creates a new set of disabled recipe IDs with it added and pushes it onto the attempts stack.
        if (disabledRecipeIds.contains(recipeIdToDisable)) {
            return;
        }

        final Set<UUID> nextDisabledRecipeIds = new LinkedHashSet<>(disabledRecipeIds);
        nextDisabledRecipeIds.add(recipeIdToDisable);
        final List<UUID> key = nextDisabledRecipeIds.stream().sorted().toList();
        if (visited.add(key)) {
            attempts.push(Set.copyOf(nextDisabledRecipeIds));
            return;
        }
    }

    private Optional<LpCraftingSolution> solveWithDisabledRecipes(final List<LpPatternRecipe> recipes,
                                                                  final LpResourceSet startingResources,
                                                                  final LpResourceSet target,
                                                                  final Set<UUID> disabledRecipeIds) {
        // Solves the crafting problem with the given set of disabled recipe IDs, returning an optional solution.
        final List<LpPatternRecipe> prioritizedRecipes =
            LpRecipeAnalysis.prioritizeAndPruneRelevantRecipes(copyRecipes(recipes), target);
        final Set<ResourceKey> relevantResources = new LinkedHashSet<>(
            LpRecipeAnalysis.collectRelevantResourceKeys(prioritizedRecipes)
        );
        relevantResources.addAll(target.resourceKeys());

        final FlowSearchResult result = new FlowSearchModel(
            prioritizedRecipes,
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
        // The actual class used for LP solving. 
        // Constructed with all the relevant recipes, resources, and constraints for a given crafting problem, 
        // and provides methods for solving it with different objectives 
        // (lexicographic minimum for required base item calculation, or maximizing target resource for max craftable amount calculation).
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
        }

        private FlowSearchResult lexicographicMinimum() {
            final FlowSearchResult feasibilityResult = solveWithObjective(null, null, false, Map.of());
            if (feasibilityResult == null) {
                return null;
            }

            final Map<UUID, Long> lockedRecipeValues = new LinkedHashMap<>();
            for (final LpPatternRecipe recipe : reversePriorityRecipes) {
                final FlowSearchResult result = solveWithObjective(null, recipe.uniqueId(), false, lockedRecipeValues);
                Objects.requireNonNull(result, "Expected lexicographic lock step to remain feasible");
                lockedRecipeValues.put(recipe.uniqueId(), result.recipeValues().getOrDefault(recipe.uniqueId(), 0L));
            }
            final FlowSearchResult finalResult = solveWithObjective(null, null, false, lockedRecipeValues);
            return finalResult;
        }

        private FlowSearchResult maximize(final ResourceKey objectiveResource) {
            final FlowSearchResult result = solveWithObjective(
                Objects.requireNonNull(objectiveResource, "objectiveResource cannot be null"),
                null,
                true,
                Map.of()
            );
            return result;
        }

        private FlowSearchResult solveWithObjective(final ResourceKey objectiveResource,
                                                    final UUID objectiveRecipeId,
                                                    final boolean maximize,
                                                    final Map<UUID, Long> lockedRecipeValues) {
            final ExpressionsBasedModel model = new ExpressionsBasedModel();
            final Map<UUID, Variable> variableByRecipeId = createRecipeVariables(model);
            configureObjective(model, variableByRecipeId, objectiveResource, objectiveRecipeId);
            addResourceConstraints(model, variableByRecipeId);
            addRecipeLocks(model, variableByRecipeId, lockedRecipeValues);

            final Optimisation.Result result = maximize ? model.maximise() : model.minimise();
            if (!result.getState().isFeasible()) {
                return null;
            }

            final Map<UUID, Long> recipeValues = extractUsedRecipeValues(variableByRecipeId);

            final LpResourceSet finalInventoryValues = computeFinalInventoryValues(recipeValues);

            return new FlowSearchResult(
                Map.copyOf(recipeValues),
                finalInventoryValues.copy()
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
        }

        private void addRecipeLocks(final ExpressionsBasedModel model,
                                    final Map<UUID, Variable> variableByRecipeId,
                                    final Map<UUID, Long> lockedRecipeValues) {
            for (final Map.Entry<UUID, Long> lock : lockedRecipeValues.entrySet()) {
                final Expression lockExpression = model.newExpression("lock:" + lock.getKey());
                lockExpression.level(lock.getValue());
                lockExpression.set(variableByRecipeId.get(lock.getKey()), 1);
            }
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

        private LpResourceSet computeFinalInventoryValues(final Map<UUID, Long> recipeValues) {
            final LpResourceSet finalInventoryValues = LpResourceSet.empty();
            for (final ResourceKey resource : relevantResources) {
                long amount = startingResources.getAmount(resource);
                for (final LpPatternRecipe recipe : recipes) {
                    final long usage = recipeValues.getOrDefault(recipe.uniqueId(), 0L);
                    if (usage == 0) {
                        continue;
                    }
                    amount += recipe.coefficient(resource) * usage;
                }
                finalInventoryValues.setAmount(resource, amount);
            }
            return finalInventoryValues;
        }
    }

    private record FlowSearchResult(Map<UUID, Long> recipeValues, LpResourceSet finalInventoryValues) {
    }
}
