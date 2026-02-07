package com.refinedmods.refinedstorage.neoforge.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.neoforge.api.ResourceHandlerExternalPatternSinkStrategy;
import com.refinedmods.refinedstorage.neoforge.storage.ResourceHandlerProvider;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;

class ResourceHandlerExternalPatternSinkStrategyImpl<T extends Resource>
    implements ResourceHandlerExternalPatternSinkStrategy {
    private final ResourceHandlerProvider<T> provider;
    private final Function<ResourceKey, Optional<T>> toPlatformMapper;

    ResourceHandlerExternalPatternSinkStrategyImpl(
        final ResourceHandlerProvider<T> provider,
        final Function<ResourceKey, Optional<T>> toPlatformMapper
    ) {
        this.provider = provider;
        this.toPlatformMapper = toPlatformMapper;
    }

    @Override
    public ExternalPatternSink.Result accept(final net.neoforged.neoforge.transfer.transaction.Transaction tx,
                                             final Collection<ResourceAmount> resources) {
        boolean anyResourceWasApplicable = false;
        for (final ResourceAmount resourceAmount : resources) {
            final T platformResource = toPlatformMapper.apply(resourceAmount.resource()).orElse(null);
            if (platformResource == null) {
                continue;
            }
            anyResourceWasApplicable = true;
            final ResourceHandler<T> handler = provider.resolve().orElse(null);
            if (handler == null) {
                return ExternalPatternSink.Result.SKIPPED;
            }
            if (handler.insert(platformResource, (int) resourceAmount.amount(), tx) != resourceAmount.amount()) {
                return ExternalPatternSink.Result.REJECTED;
            }
        }
        return anyResourceWasApplicable
            ? ExternalPatternSink.Result.ACCEPTED
            : ExternalPatternSink.Result.SKIPPED;
    }

    @Override
    public boolean isEmpty() {
        return provider.resolve().map(handler -> {
            for (int i = 0; i < handler.size(); ++i) {
                final T resource = handler.getResource(i);
                if (!resource.isEmpty()) {
                    return false;
                }
            }
            return true;
        }).orElse(true);
    }

    @Override
    public boolean applies(final ResourceKey resource) {
        return toPlatformMapper.apply(resource).isPresent();
    }
}
