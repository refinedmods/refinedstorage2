package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public class MutableTaskPlan {
    @Nullable
    private final Pattern rootPattern;
    @Nullable
    private final ResourceKey resource;
    private final long amount;
    private final Map<Pattern, MutablePatternPlan> patterns;
    private final MutableResourceList initialRequirements;
    private boolean missing;

    MutableTaskPlan() {
        this(null, null, 0, new LinkedHashMap<>(), MutableResourceListImpl.create(), false);
    }

    private MutableTaskPlan(@Nullable final Pattern rootPattern,
                            @Nullable final ResourceKey resource,
                            final long amount,
                            final Map<Pattern, MutablePatternPlan> patterns,
                            final MutableResourceList initialRequirements,
                            final boolean missing) {
        this.rootPattern = rootPattern;
        this.resource = resource;
        this.amount = amount;
        this.patterns = patterns;
        this.initialRequirements = initialRequirements;
        this.missing = missing;
    }

    void addOrUpdatePattern(final Pattern usedPattern, final long iterations) {
        patterns.computeIfAbsent(usedPattern, p -> new MutablePatternPlan(
            p,
            p.equals(rootPattern)
        )).addIterations(iterations);
    }

    void addToExtract(final ResourceKey toExtract, final long toExtractAmount) {
        initialRequirements.add(toExtract, toExtractAmount);
    }

    void addUsedIngredient(final Pattern ingredientPattern,
                           final int ingredientIndex,
                           final ResourceKey ingredient,
                           final long ingredientAmount) {
        final MutablePatternPlan patternPlan = requireNonNull(patterns.get(ingredientPattern));
        patternPlan.addUsedIngredient(ingredientIndex, ingredient, ingredientAmount);
    }

    MutableTaskPlan copy(final Pattern childPattern, final ResourceKey childResource, final long totalAmount) {
        final Map<Pattern, MutablePatternPlan> patternsCopy = new LinkedHashMap<>();
        for (final Map.Entry<Pattern, MutablePatternPlan> entry : patterns.entrySet()) {
            patternsCopy.put(entry.getKey(), entry.getValue().copy());
        }
        return new MutableTaskPlan(
            rootPattern == null ? childPattern : rootPattern,
            resource == null ? childResource : resource,
            resource == null ? totalAmount : amount,
            patternsCopy,
            initialRequirements.copy(),
            missing
        );
    }

    Optional<TaskPlan> getPlan() {
        if (missing || rootPattern == null || resource == null) {
            return Optional.empty();
        }
        final Map<Pattern, TaskPlan.PatternPlan> finalPatterns = Collections.unmodifiableMap(patterns.entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().getPlan(),
                (a, b) -> a,
                LinkedHashMap::new
            )));
        return Optional.of(new TaskPlan(resource, amount, rootPattern, finalPatterns, initialRequirements.copyState()));
    }

    void setMissing() {
        this.missing = true;
    }
}
