package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternLayout;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

class ExternalPatternSinkProviderImpl implements ExternalPatternSinkProvider {
    private final Map<PatternLayout, ExternalPatternSink> sinks = new HashMap<>();

    ExternalPatternSinkImpl put(final Pattern pattern) {
        return put(pattern, new ExternalPatternSinkImpl(new StorageImpl(MutableResourceListImpl.orderPreserving()),
            pattern, null));
    }

    void put(final Pattern pattern, final ExternalPatternSink.Result fixedResult) {
        put(pattern, new ExternalPatternSinkImpl(new StorageImpl(MutableResourceListImpl.orderPreserving()), pattern,
            fixedResult));
    }

    <T extends ExternalPatternSink> T put(final Pattern pattern, final T sink) {
        sinks.put(pattern.layout(), sink);
        return sink;
    }

    void remove(final Pattern pattern) {
        sinks.remove(pattern.layout());
    }

    static ExternalPatternSinkKey sinkKey(final Pattern pattern) {
        return new ExternalPatternSinkKeyImpl(pattern);
    }

    @Override
    public List<ExternalPatternSink> getSinksByPatternLayout(final PatternLayout patternLayout) {
        final ExternalPatternSink sink = sinks.get(patternLayout);
        if (sink == null) {
            return List.of();
        }
        return List.of(sink);
    }

    static class ExternalPatternSinkImpl implements ExternalPatternSink {
        private final Storage storage;
        private final Pattern pattern;
        @Nullable
        private final Result fixedResult;

        private ExternalPatternSinkImpl(final Storage storage,
                                        final Pattern pattern,
                                        @Nullable final Result fixedResult) {
            this.storage = storage;
            this.pattern = pattern;
            this.fixedResult = fixedResult;
        }

        Collection<ResourceAmount> getAll() {
            return storage.getAll();
        }

        @Nullable
        @Override
        public ExternalPatternSinkKey getKey() {
            return new ExternalPatternSinkKeyImpl(pattern);
        }

        @Override
        public Result accept(final Pattern p,
                             final Collection<ResourceAmount> resources,
                             final Action action) {
            if (fixedResult != null) {
                return fixedResult;
            }
            if (action == Action.EXECUTE) {
                return accept(resources);
            }
            return acceptsSimulated(resources);
        }

        private Result accept(final Collection<ResourceAmount> resources) {
            for (final ResourceAmount resourceAmount : resources) {
                final long inserted = storage.insert(
                    resourceAmount.resource(), resourceAmount.amount(), Action.EXECUTE, Actor.EMPTY
                );
                if (inserted != resourceAmount.amount()) {
                    throw new IllegalStateException();
                }
            }
            return Result.ACCEPTED;
        }

        private Result acceptsSimulated(final Collection<ResourceAmount> resources) {
            final Storage storageCopy = copyStorage();
            for (final ResourceAmount resourceAmount : resources) {
                final long inserted = storageCopy.insert(
                    resourceAmount.resource(),
                    resourceAmount.amount(),
                    Action.EXECUTE,
                    Actor.EMPTY
                );
                if (inserted != resourceAmount.amount()) {
                    return Result.REJECTED;
                }
            }
            return Result.ACCEPTED;
        }

        private Storage copyStorage() {
            final Storage storageCopy = new StorageImpl();
            storage.getAll().forEach(r -> storageCopy.insert(r.resource(), r.amount(), Action.EXECUTE, Actor.EMPTY));
            return storageCopy;
        }
    }

    private record ExternalPatternSinkKeyImpl(Pattern pattern) implements ExternalPatternSinkKey {
    }
}
