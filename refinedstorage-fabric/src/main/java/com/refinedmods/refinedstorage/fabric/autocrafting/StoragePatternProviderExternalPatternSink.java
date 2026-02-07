package com.refinedmods.refinedstorage.fabric.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.autocrafting.PlatformPatternProviderExternalPatternSink;
import com.refinedmods.refinedstorage.fabric.api.StorageExternalPatternSinkStrategy;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

class StoragePatternProviderExternalPatternSink implements PlatformPatternProviderExternalPatternSink {
    private final Set<StorageExternalPatternSinkStrategy> strategies;

    StoragePatternProviderExternalPatternSink(final Set<StorageExternalPatternSinkStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public ExternalPatternSink.Result accept(final Collection<ResourceAmount> resources, final Action action) {
        ExternalPatternSink.Result result = ExternalPatternSink.Result.SKIPPED;
        try (Transaction tx = Transaction.openOuter()) {
            final Set<StorageExternalPatternSinkStrategy> sortedStrategies = getSortedStrategies(resources);
            for (final StorageExternalPatternSinkStrategy strategy : sortedStrategies) {
                final ExternalPatternSink.Result strategyResult = strategy.accept(tx, resources);
                if (strategyResult == ExternalPatternSink.Result.REJECTED) {
                    return strategyResult;
                }
                result = and(result, strategyResult);
            }
            if (action == Action.EXECUTE) {
                tx.commit();
            }
        }
        return result;
    }

    private Set<StorageExternalPatternSinkStrategy> getSortedStrategies(final Collection<ResourceAmount> resources) {
        final Set<ResourceKey> order = resources.stream()
            .map(ResourceAmount::resource)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        return order.stream()
            .flatMap(resource -> strategies.stream().filter(s -> s.applies(resource)))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private ExternalPatternSink.Result and(final ExternalPatternSink.Result a,
                                           final ExternalPatternSink.Result b) {
        if (a == ExternalPatternSink.Result.SKIPPED) {
            return b;
        } else if (a == ExternalPatternSink.Result.REJECTED || b == ExternalPatternSink.Result.REJECTED) {
            return ExternalPatternSink.Result.REJECTED;
        } else {
            return ExternalPatternSink.Result.ACCEPTED;
        }
    }

    @Override
    public boolean isEmpty() {
        for (final StorageExternalPatternSinkStrategy strategy : strategies) {
            if (!strategy.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
