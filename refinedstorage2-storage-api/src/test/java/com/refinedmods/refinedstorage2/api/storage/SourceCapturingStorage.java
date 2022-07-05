package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;

import java.util.ArrayList;
import java.util.List;

public class SourceCapturingStorage<T> extends AbstractProxyStorage<T> {
    private final List<Source> sourcesUsed = new ArrayList<>();

    public SourceCapturingStorage(final Storage<T> delegate) {
        super(delegate);
    }

    @Override
    public long extract(final T resource, final long amount, final Action action, final Source source) {
        sourcesUsed.add(source);
        return super.extract(resource, amount, action, source);
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Source source) {
        sourcesUsed.add(source);
        return super.insert(resource, amount, action, source);
    }

    public List<Source> getSourcesUsed() {
        return sourcesUsed;
    }
}
