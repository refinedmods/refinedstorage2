package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;

import java.util.ArrayList;
import java.util.List;

public class SourceCapturingStorage<T> extends AbstractProxyStorage<T> {
    private final List<Source> sourcesUsed = new ArrayList<>();

    public SourceCapturingStorage(Storage<T> delegate) {
        super(delegate);
    }

    @Override
    public long extract(T resource, long amount, Action action, Source source) {
        sourcesUsed.add(source);
        return super.extract(resource, amount, action, source);
    }

    @Override
    public long insert(T resource, long amount, Action action, Source source) {
        sourcesUsed.add(source);
        return super.insert(resource, amount, action, source);
    }

    public List<Source> getSourcesUsed() {
        return sourcesUsed;
    }
}
