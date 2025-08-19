package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

class MutablePatternPlan {
    private final Pattern pattern;
    private final boolean root;
    private final Map<Integer, Map<ResourceKey, Long>> ingredients = new HashMap<>();
    private long iterations;

    MutablePatternPlan(final Pattern pattern, final boolean root) {
        this.pattern = pattern;
        this.root = root;
    }

    void addIterations(final long it) {
        this.iterations += it;
    }

    void addUsedIngredient(final int ingredientIndex, final ResourceKey resource, final long amount) {
        final Map<ResourceKey, Long> resources = ingredients.computeIfAbsent(
            ingredientIndex,
            i -> new LinkedHashMap<>()
        );
        resources.put(resource, resources.getOrDefault(resource, 0L) + amount);
    }

    MutablePatternPlan copy() {
        final MutablePatternPlan copy = new MutablePatternPlan(pattern, root);
        copy.iterations = iterations;
        for (final Map.Entry<Integer, Map<ResourceKey, Long>> entry : ingredients.entrySet()) {
            final Map<ResourceKey, Long> resourcesCopy = new LinkedHashMap<>(entry.getValue());
            copy.ingredients.put(entry.getKey(), resourcesCopy);
        }
        return copy;
    }

    TaskPlan.PatternPlan getPlan() {
        final Map<Integer, Map<ResourceKey, Long>> orderPreservingIngredients =
            Collections.unmodifiableMap(ingredients.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> Collections.unmodifiableMap(e.getValue()
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                    ))),
                (a, b) -> a,
                LinkedHashMap::new
            )));
        return new TaskPlan.PatternPlan(root, iterations, orderPreservingIngredients);
    }
}
