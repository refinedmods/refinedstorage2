package com.refinedmods.refinedstorage.api.resource.list;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collection;
import java.util.Set;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

/**
 * This is a utility class to easily decorate a {@link MutableResourceListImpl}.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public abstract class AbstractProxyMutableResourceList implements MutableResourceList {
    private final MutableResourceList delegate;

    protected AbstractProxyMutableResourceList(final MutableResourceList delegate) {
        this.delegate = delegate;
    }

    @Override
    public OperationResult add(final ResourceKey resource, final long amount) {
        return delegate.add(resource, amount);
    }

    @Override
    @Nullable
    public OperationResult remove(final ResourceKey resource, final long amount) {
        return delegate.remove(resource, amount);
    }

    @Override
    public Collection<ResourceAmount> copyState() {
        return delegate.copyState();
    }

    @Override
    public long get(final ResourceKey resource) {
        return delegate.get(resource);
    }

    @Override
    public boolean contains(final ResourceKey resource) {
        return delegate.contains(resource);
    }

    @Override
    public Set<ResourceKey> getAll() {
        return delegate.getAll();
    }

    @Override
    public MutableResourceList copy() {
        return delegate.copy();
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
