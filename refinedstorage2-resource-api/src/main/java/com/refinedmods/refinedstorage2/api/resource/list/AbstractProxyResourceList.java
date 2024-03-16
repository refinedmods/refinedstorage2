package com.refinedmods.refinedstorage2.api.resource.list;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;

import java.util.Collection;
import java.util.Optional;

import org.apiguardian.api.API;

/**
 * This is a utility class to easily decorate a {@link ResourceList}.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public abstract class AbstractProxyResourceList implements ResourceList {
    private final ResourceList delegate;

    protected AbstractProxyResourceList(final ResourceList delegate) {
        this.delegate = delegate;
    }

    @Override
    public OperationResult add(final ResourceKey resource, final long amount) {
        return delegate.add(resource, amount);
    }

    @Override
    public Optional<OperationResult> remove(final ResourceKey resource, final long amount) {
        return delegate.remove(resource, amount);
    }

    @Override
    public Optional<ResourceAmount> get(final ResourceKey resource) {
        return delegate.get(resource);
    }

    @Override
    public Collection<ResourceAmount> getAll() {
        return delegate.getAll();
    }

    @Override
    public void clear() {
        delegate.clear();
    }
}
