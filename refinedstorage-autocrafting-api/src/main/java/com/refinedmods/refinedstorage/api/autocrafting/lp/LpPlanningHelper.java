package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternRepositoryImpl;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper utility for LP (Linear Programming) planning decisions.
 * Contains logic to determine when to use the LP system vs traditional system.
 */
public final class LpPlanningHelper {
    private LpPlanningHelper() {
    }

    /**
     * Determines whether to use the LP system for a given resource.
     * 
     * Returns true if all ingredients in the crafting tree have exactly one viable input
     * (i.e., in storage or craftable via a pattern). If an ingredient has multiple
     * possible inputs but only one is actually available or craftable, it is not
     * considered fuzzy. Returns false (use traditional system) only when multiple viable
     * alternatives exist for the same ingredient.
     */
    public static boolean shouldUseLPSystem(final ResourceKey requestedResource,
                                     final RootStorage rootStorage,
                                     final PatternRepositoryImpl patternRepository) {
        final Set<ResourceKey> visitedResources = new HashSet<>();
        final ArrayDeque<ResourceKey> resourcesToVisit = new ArrayDeque<>();
        resourcesToVisit.add(requestedResource);

        while (!resourcesToVisit.isEmpty()) {
            final ResourceKey currentResource = resourcesToVisit.removeFirst();
            if (!visitedResources.add(currentResource)) {
                continue;
            }

            for (final Pattern pattern : patternRepository.getByOutput(currentResource)) {
                for (final var ingredient : pattern.layout().ingredients()) {
                    if (ingredient.inputs().size() == 1) {
                        resourcesToVisit.addLast(ingredient.inputs().getFirst());
                        continue;
                    }
                    // Multiple possible inputs: only count those available in storage or craftable
                    final List<ResourceKey> viableInputs = ingredient.inputs().stream()
                        .filter(input -> rootStorage.get(input) > 0
                            || !patternRepository.getByOutput(input).isEmpty())
                        .toList();
                    if (viableInputs.size() > 1) {
                        return false;
                    }
                    if (!viableInputs.isEmpty()) {
                        resourcesToVisit.addLast(viableInputs.getFirst());
                    }
                }
            }
        }

        return true;
    }

    /**
     * Collects all patterns relevant to crafting the requested resource using LP.
     */
    public static Collection<Pattern> collectRelevantPatternsForLp(final ResourceKey requestedResource,
                                                            final RootStorage rootStorage,
                                                            final PatternRepositoryImpl patternRepository) {
        final Set<Pattern> relevantPatterns = new LinkedHashSet<>();
        final Set<ResourceKey> visitedResources = new HashSet<>();
        final ArrayDeque<ResourceKey> resourcesToVisit = new ArrayDeque<>();
        resourcesToVisit.add(requestedResource);

        while (!resourcesToVisit.isEmpty()) {
            final ResourceKey currentResource = resourcesToVisit.removeFirst();
            if (!visitedResources.add(currentResource)) {
                continue;
            }

            for (final Pattern pattern : patternRepository.getByOutput(currentResource)) {
                relevantPatterns.add(pattern);
                for (final var ingredient : pattern.layout().ingredients()) {
                    if (ingredient.inputs().size() == 1) {
                        resourcesToVisit.addLast(ingredient.inputs().getFirst());
                        continue;
                    }
                    ingredient.inputs().stream()
                        .filter(input -> rootStorage.get(input) > 0 || !patternRepository.getByOutput(input).isEmpty())
                        .forEach(resourcesToVisit::addLast);
                }
            }
        }

        return relevantPatterns;
    }
}
