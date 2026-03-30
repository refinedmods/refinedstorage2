package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.C;
import static org.assertj.core.api.Assertions.assertThat;

class LpCraftingSolverFlowSearchModelTest {
    @Test
    void objectiveCoefficientShouldHandleRecipeAndResourceObjectives() throws Exception {
        // Tests that objective coefficients are correctly calculated for both resource and recipe-based objectives.
        final LpPatternRecipe recipe = recipe(A, B, 1, 2, 0);
        final Object model = flowSearchModel(
            List.of(recipe),
            Set.of(A, B),
            set(A, 3),
            set(B, 1),
            Set.of(A, B),
            Set.of()
        );

        final long byResource = invokeObjectiveCoefficient(model, recipe, B, null);
        final long byRecipeMatch = invokeObjectiveCoefficient(model, recipe, C, recipe.uniqueId());
        final long byRecipeNoMatch = invokeObjectiveCoefficient(model, recipe, C, UUID.randomUUID());

        assertThat(byResource).isEqualTo(2L);
        assertThat(byRecipeMatch).isEqualTo(1L);
        assertThat(byRecipeNoMatch).isZero();
    }

    @Test
    void solveWithObjectiveShouldReturnFeasibleAndInfeasibleResults() throws Exception {
        // Tests that the solver returns feasible solutions when recipes are available and null when disabled.
        final LpPatternRecipe recipe = recipe(A, B, 1, 1, 0);

        final Object feasibleModel = flowSearchModel(
            List.of(recipe),
            Set.of(A, B),
            set(A, 2),
            set(B, 1),
            Set.of(A, B),
            Set.of()
        );
        final Object feasible = invokeSolveWithObjective(feasibleModel, null, null, false, Map.of());
        assertThat(feasible).isNotNull();
        assertThat(recipeValues(feasible)).containsKey(recipe.uniqueId());

        final Object disabledModel = flowSearchModel(
            List.of(recipe),
            Set.of(A, B),
            set(A, 2),
            set(B, 1),
            Set.of(A, B),
            Set.of(recipe.uniqueId())
        );
        final Object infeasible = invokeSolveWithObjective(disabledModel, null, null, false, Map.of());
        assertThat(infeasible).isNull();
    }

    @Test
    void maximizeAndLexicographicMinimumShouldProduceConsistentInventory() throws Exception {
        // Tests that maximize and lexicographic minimum operations produce consistent final inventory states.
        final LpPatternRecipe recipe = recipe(A, B, 1, 1, 0);
        final Object model = flowSearchModel(
            List.of(recipe),
            Set.of(A, B),
            set(A, 3),
            set(B, 1),
            Set.of(A, B),
            Set.of()
        );

        final Object maxResult = invokeMaximize(model, B);
        final Object lexResult = invokeLexicographicMinimum(model);

        assertThat(maxResult).isNotNull();
        assertThat(lexResult).isNotNull();
        assertThat(finalInventoryValues(maxResult).getAmount(B))
            .isGreaterThanOrEqualTo(1L);
        assertThat(finalInventoryValues(lexResult).getAmount(B))
            .isGreaterThanOrEqualTo(1L);
    }

    @Test
    void lexicographicMinimumShouldMinimizeLowerEffectivePriorityRecipesFirst() throws Exception {
        // Lower effective-priority recipes are minimized first.
        final LpPatternRecipe low = recipe(A, B, 1, 1, 0);
        final LpPatternRecipe high = recipe(A, B, 1, 1, 0);
        low.setEffectivePriority(0);
        high.setEffectivePriority(10);

        final Object model = flowSearchModel(
            List.of(low, high),
            Set.of(A, B),
            set(A, 1),
            set(B, 1),
            Set.of(A, B),
            Set.of()
        );

        final Object result = invokeLexicographicMinimum(model);

        assertThat(result).isNotNull();
        assertThat(recipeValues(result).getOrDefault(high.uniqueId(), 0L))
            .isEqualTo(1L);
        assertThat(recipeValues(result).getOrDefault(low.uniqueId(), 0L)).isZero();
    }

    private static LpPatternRecipe recipe(final ResourceKey in,
                                          final ResourceKey out,
                                          final long inAmount,
                                          final long outAmount,
                                          final int priority) {
        return LpPatternRecipe.fromPattern(
            PatternBuilder.pattern().ingredient(in, inAmount).output(out, outAmount).build(),
            priority
        );
    }

    private static LpResourceSet set(final Object... entries) {
        final LpResourceSet set = new LpResourceSet();
        for (int i = 0; i < entries.length; i += 2) {
            set.setAmount((ResourceKey) entries[i], ((Number) entries[i + 1]).longValue());
        }
        return set;
    }

    private static Object flowSearchModel(final List<LpPatternRecipe> recipes,
                                          final Set<ResourceKey> relevant,
                                          final LpResourceSet starting,
                                          final LpResourceSet target,
                                          final Set<ResourceKey> constrained,
                                          final Set<UUID> disabled) throws Exception {
        final Class<?> flowClass =
            Class.forName("com.refinedmods.refinedstorage.api.autocrafting.lp.LpCraftingSolver$FlowSearchModel");
        final Constructor<?> constructor = flowClass.getDeclaredConstructor(
            List.class,
            Set.class,
            LpResourceSet.class,
            LpResourceSet.class,
            Set.class,
            Set.class,
            LpSolverOptions.class
        );
        constructor.setAccessible(true);
        return constructor.newInstance(
            recipes,
            relevant,
            starting,
            target,
            constrained,
            disabled,
            new LpSolverOptions(100, 50)
        );
    }

    private static long invokeObjectiveCoefficient(final Object model,
                                                   final LpPatternRecipe recipe,
                                                   final ResourceKey objectiveResource,
                                                   final UUID objectiveRecipeId) throws Exception {
        final Method method = model.getClass().getDeclaredMethod(
            "objectiveCoefficient",
            LpPatternRecipe.class,
            ResourceKey.class,
            UUID.class
        );
        method.setAccessible(true);
        return (long) method.invoke(model, recipe, objectiveResource, objectiveRecipeId);
    }

    private static Object invokeSolveWithObjective(final Object model,
                                                   final ResourceKey objectiveResource,
                                                   final UUID objectiveRecipeId,
                                                   final boolean maximize,
                                                   final Map<UUID, Long> locks) throws Exception {
        final Method method = model.getClass().getDeclaredMethod(
            "solveWithObjective",
            ResourceKey.class,
            UUID.class,
            boolean.class,
            Map.class
        );
        method.setAccessible(true);
        return method.invoke(model, objectiveResource, objectiveRecipeId, maximize, locks);
    }

    private static Object invokeMaximize(final Object model, final ResourceKey objectiveResource) throws Exception {
        final Method method = model.getClass().getDeclaredMethod("maximize", ResourceKey.class);
        method.setAccessible(true);
        return method.invoke(model, objectiveResource);
    }

    private static Object invokeLexicographicMinimum(final Object model) throws Exception {
        final Method method = model.getClass().getDeclaredMethod("lexicographicMinimum");
        method.setAccessible(true);
        return method.invoke(model);
    }

    @SuppressWarnings("unchecked")
    private static Map<UUID, Long> recipeValues(final Object flowResult) throws Exception {
        final Method method = flowResult.getClass().getDeclaredMethod("recipeValues");
        method.setAccessible(true);
        return (Map<UUID, Long>) method.invoke(flowResult);
    }

    private static LpResourceSet finalInventoryValues(final Object flowResult) throws Exception {
        final Method method = flowResult.getClass().getDeclaredMethod("finalInventoryValues");
        method.setAccessible(true);
        return (LpResourceSet) method.invoke(flowResult);
    }
}
