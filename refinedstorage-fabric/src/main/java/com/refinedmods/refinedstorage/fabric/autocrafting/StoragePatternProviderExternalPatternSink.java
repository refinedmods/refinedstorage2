package com.refinedmods.refinedstorage.fabric.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.autocrafting.PlatformPatternProviderExternalPatternSink;
import com.refinedmods.refinedstorage.fabric.api.StorageExternalPatternSinkStrategy;

import java.util.Collection;
import java.util.Map;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

class StoragePatternProviderExternalPatternSink implements PlatformPatternProviderExternalPatternSink {
    private final Map<Class<? extends ResourceKey>, StorageExternalPatternSinkStrategy> strategies;

    StoragePatternProviderExternalPatternSink(
        final Map<Class<? extends ResourceKey>, StorageExternalPatternSinkStrategy> strategies
    ) {
        this.strategies = strategies;
    }

    @Override
    public ExternalPatternSink.Result insertAll(final Collection<ResourceAmount> resources, final Action action) {
        ExternalPatternSink.Result result = ExternalPatternSink.Result.SKIPPED;
        try (Transaction tx = Transaction.openOuter()) {
            for (final ResourceAmount resourceAmount : resources) {
                final Class<? extends ResourceKey> resourceType = resourceAmount.resource().getClass();
                final StorageExternalPatternSinkStrategy strategy = strategies.get(resourceType);
                if (strategy == null) {
                    continue;
                }
                final ExternalPatternSink.Result strategyResult = strategy.insert(tx, resourceAmount);
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
        for (final StorageExternalPatternSinkStrategy strategy : strategies.values()) {
            if (!strategy.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
