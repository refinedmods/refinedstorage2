package com.refinedmods.refinedstorage.api.network.impl.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpCraftingSolver;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpExecutionPlanStep;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpPatternRecipe;
import com.refinedmods.refinedstorage.api.autocrafting.lp.LpResourceSet;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;

final class LpStepPlanCalculator {
    private LpStepPlanCalculator() {
    }

    static Optional<LpStepPlan> calculateSteps(final Collection<Pattern> patterns,
                                               final Logger logger,
                                               final RootStorage rootStorage,
                                               final ResourceKey resource,
                                               final long amount,
                                               final CancellationToken cancellationToken) {
        if (cancellationToken.isCancelled()) {
            return Optional.empty();
        }

        final List<LpPatternRecipe> recipes = buildLpRecipes(patterns, logger);
        if (recipes.isEmpty()) {
            return Optional.empty();
        }

        final LpCraftingSolver.PlanningOutcome outcome = new LpCraftingSolver().solve(
            recipes,
            buildLpStartingResources(rootStorage),
            buildTarget(rootStorage, resource, amount)
        );
        if (outcome.executableResult().isEmpty()) {
            return Optional.empty();
        }
        final List<LpExecutionPlanStep> steps = outcome.executableResult().get().plan();
        if (steps.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new LpStepPlan(steps, hasRecipeCycles(steps)));
    }

    private static List<LpPatternRecipe> buildLpRecipes(final Collection<Pattern> patterns, final Logger logger) {
        final List<Pattern> sorted = patterns.stream()
            .sorted(Comparator.comparing(Pattern::id))
            .toList();
        final List<LpPatternRecipe> recipes = new ArrayList<>();
        for (int index = 0; index < sorted.size(); index++) {
            try {
                recipes.add(LpPatternRecipe.fromPattern(sorted.get(index), index));
            } catch (final IllegalArgumentException e) {
                logger.debug("Skipping LP-incompatible pattern {}", sorted.get(index), e);
            }
        }
        return recipes;
    }

    private static LpResourceSet buildLpStartingResources(final RootStorage rootStorage) {
        return LpResourceSet.fromResourceAmounts(rootStorage.getAll());
    }

    private static LpResourceSet buildTarget(final RootStorage rootStorage,
                                             final ResourceKey resource,
                                             final long amount) {
        final LpResourceSet target = new LpResourceSet();
        target.setAmount(resource, rootStorage.get(resource) + amount);
        return target;
    }

    static boolean hasRecipeCycles(final List<LpExecutionPlanStep> steps) {
        final List<Pattern> patterns = steps.stream()
            .map(step -> step.recipe().pattern())
            .distinct()
            .toList();
        if (patterns.isEmpty()) {
            return false;
        }

        final Map<Pattern, Set<ResourceKey>> producedByPattern = new HashMap<>();
        final Map<Pattern, Set<ResourceKey>> consumedByPattern = new HashMap<>();

        for (final Pattern pattern : patterns) {
            final Set<ResourceKey> produced = new HashSet<>();
            pattern.layout().outputs().forEach(output -> produced.add(output.resource()));
            pattern.layout().byproducts().forEach(byproduct -> produced.add(byproduct.resource()));
            producedByPattern.put(pattern, produced);

            final Set<ResourceKey> consumed = new HashSet<>();
            pattern.layout().ingredients().forEach(ingredient -> consumed.add(ingredient.inputs().getFirst()));
            consumedByPattern.put(pattern, consumed);
        }

        final Map<Pattern, Set<Pattern>> dependencies = new HashMap<>();
        for (final Pattern pattern : patterns) {
            dependencies.putIfAbsent(pattern, new HashSet<>());
            final Set<ResourceKey> produced = producedByPattern.get(pattern);
            for (final Pattern target : patterns) {
                final Set<ResourceKey> consumed = consumedByPattern.get(target);
                if (produced.stream().anyMatch(consumed::contains)) {
                    dependencies.get(pattern).add(target);
                }
            }
        }

        final Set<Pattern> visiting = new HashSet<>();
        final Set<Pattern> visited = new HashSet<>();
        for (final Pattern pattern : patterns) {
            if (containsCycle(pattern, dependencies, visiting, visited)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsCycle(final Pattern current,
                                         final Map<Pattern, Set<Pattern>> dependencies,
                                         final Set<Pattern> visiting,
                                         final Set<Pattern> visited) {
        if (visited.contains(current)) {
            return false;
        }
        if (!visiting.add(current)) {
            return true;
        }
        for (final Pattern dependency : dependencies.getOrDefault(current, Collections.emptySet())) {
            if (containsCycle(dependency, dependencies, visiting, visited)) {
                return true;
            }
        }
        visiting.remove(current);
        visited.add(current);
        return false;
    }
}
