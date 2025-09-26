package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProviderExternalPatternSink;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.autocrafting.PlatformPatternProviderExternalPatternSink;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

class CompositePatternProviderExternalPatternSink
    implements PlatformPatternProviderExternalPatternSink {
    private final Set<PlatformPatternProviderExternalPatternSink> sinks;

    CompositePatternProviderExternalPatternSink(final Set<PlatformPatternProviderExternalPatternSink> sinks) {
        this.sinks = sinks;
    }

    @Override
    public ExternalPatternSink.Result accept(final Collection<ResourceAmount> resources, final Action action) {
        // Sinks insert a specific resource type.
        // To preserve the resource order defined in the pattern, sinks are sorted by resource type first.
        // Limitation 1: with interleaved types (e.g. item–fluid–item–fluid),
        // the resulting order still will not match the pattern. To fix that we'd have to insert resources one by one,
        // but that is difficult as NeoForge does not have transactions (yet).
        // Limitation 2 (NeoForge): with mixed resource types, we can't guarantee that a single insert will succeed
        // completely as the heuristic for checking if everything can be inserted completely is per sink,
        // not for everything at once (and only for items as of now). Transactions will also solve this.
        final Set<PatternProviderExternalPatternSink> sortedSinks = getSortedSinks(resources);
        ExternalPatternSink.Result result = ExternalPatternSink.Result.SKIPPED;
        for (final PatternProviderExternalPatternSink sink : sortedSinks) {
            final ExternalPatternSink.Result sinkResult = sink.accept(resources, action);
            if (sinkResult == ExternalPatternSink.Result.REJECTED) {
                return sinkResult;
            }
            result = and(result, sinkResult);
        }
        return result;
    }

    private Set<PatternProviderExternalPatternSink> getSortedSinks(final Collection<ResourceAmount> resources) {
        final Set<ResourceKey> order = resources.stream()
            .map(ResourceAmount::resource)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        return order.stream()
            .flatMap(resource -> sinks.stream().filter(sink -> sink.applies(resource)))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private ExternalPatternSink.Result and(final ExternalPatternSink.Result a, final ExternalPatternSink.Result b) {
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
        for (final PlatformPatternProviderExternalPatternSink sink : sinks) {
            if (!sink.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
