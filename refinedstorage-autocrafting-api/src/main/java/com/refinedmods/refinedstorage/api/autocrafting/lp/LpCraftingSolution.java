package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Solved recipe usages and end-state inventory for the standalone LP prototype.
 */
public record LpCraftingSolution(Map<UUID, Long> recipeValues,
                                 Map<ResourceKey, Long> finalInventoryValues,
                                 List<ResourceKey> relevantResourceKeys) {
    public LpCraftingSolution {
        Objects.requireNonNull(recipeValues, "recipeValues cannot be null");
        Objects.requireNonNull(finalInventoryValues, "finalInventoryValues cannot be null");
        Objects.requireNonNull(relevantResourceKeys, "relevantResourceKeys cannot be null");
        recipeValues = Map.copyOf(new LinkedHashMap<>(recipeValues));
        finalInventoryValues = Map.copyOf(new LinkedHashMap<>(finalInventoryValues));
        relevantResourceKeys = List.copyOf(relevantResourceKeys);
    }

    public long recipeUsageCount(final LpPatternRecipe recipe) {
        return recipeValues.getOrDefault(recipe.uniqueId(), 0L);
    }

    public long finalInventoryCount(final ResourceKey resource) {
        return finalInventoryValues.getOrDefault(resource, 0L);
    }
}
