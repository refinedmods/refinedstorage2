package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusBuilder;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractTaskPattern {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTaskPattern.class);

    protected final boolean root;
    protected final Pattern pattern;
    protected final Map<Integer, Map<ResourceKey, Long>> ingredients = new LinkedHashMap<>();

    protected AbstractTaskPattern(final Pattern pattern, final TaskPlan.PatternPlan plan) {
        this.pattern = pattern;
        this.root = plan.root();
        for (final Map.Entry<Integer, Map<ResourceKey, Long>> entry : plan.ingredients().entrySet()) {
            final Map<ResourceKey, Long> possibilitiesCopy = new LinkedHashMap<>(entry.getValue());
            ingredients.put(entry.getKey(), possibilitiesCopy);
        }
    }

    abstract PatternStepResult step(MutableResourceList internalStorage,
                                    RootStorage rootStorage,
                                    ExternalPatternSinkProvider sinkProvider,
                                    TaskListener listener);

    abstract TaskSnapshot.PatternSnapshot createSnapshot();

    abstract void appendStatus(TaskStatusBuilder builder);

    abstract long getWeight();

    abstract double getPercentageCompleted();

    protected final boolean extractAll(final ResourceList inputs,
                                       final MutableResourceList internalStorage,
                                       final Action action) {
        for (final ResourceKey inputResource : inputs.getAll()) {
            final long inputAmount = inputs.get(inputResource);
            final long inInternalStorage = internalStorage.get(inputResource);
            if (inInternalStorage < inputAmount) {
                return false;
            }
            if (action == Action.EXECUTE) {
                internalStorage.remove(inputResource, inputAmount);
                LOGGER.debug("Extracted {}x {} from internal storage", inputAmount, inputResource);
            }
        }
        return true;
    }

    protected final ResourceList calculateIterationInputs(final Action action) {
        final MutableResourceList iterationInputs = MutableResourceListImpl.orderPreserving();
        for (final Map.Entry<Integer, Map<ResourceKey, Long>> ingredient : ingredients.entrySet()) {
            final int ingredientIndex = ingredient.getKey();
            if (!calculateIterationInputs(ingredient, ingredientIndex, iterationInputs, action)) {
                throw new IllegalStateException();
            }
        }
        return iterationInputs;
    }

    private boolean calculateIterationInputs(final Map.Entry<Integer, Map<ResourceKey, Long>> ingredient,
                                             final int ingredientIndex,
                                             final MutableResourceList iterationInputs,
                                             final Action action) {
        long needed = pattern.layout().ingredients().get(ingredientIndex).amount();
        for (final Map.Entry<ResourceKey, Long> possibility : ingredient.getValue().entrySet()) {
            final long available = Math.min(needed, possibility.getValue());
            if (available == 0) {
                continue;
            }
            iterationInputs.add(possibility.getKey(), available);
            if (action == Action.EXECUTE) {
                possibility.setValue(possibility.getValue() - available);
            }
            needed -= available;
            if (needed == 0) {
                break;
            }
        }
        return needed == 0;
    }

    long beforeInsert(final ResourceKey resource, final long amount) {
        return 0;
    }

    long afterInsert(final ResourceKey resource, final long amount) {
        return 0;
    }
}
