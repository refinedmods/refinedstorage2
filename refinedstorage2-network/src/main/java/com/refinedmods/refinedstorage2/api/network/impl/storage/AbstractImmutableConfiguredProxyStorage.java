package com.refinedmods.refinedstorage2.api.network.impl.storage;

import com.refinedmods.refinedstorage2.api.storage.Storage;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public abstract class AbstractImmutableConfiguredProxyStorage<S extends Storage>
    extends AbstractConfiguredProxyStorage<S> {
    private static final String ERROR_MESSAGE = "Cannot modify immutable proxy";

    protected AbstractImmutableConfiguredProxyStorage(final StorageConfiguration config, final S delegate) {
        super(config, delegate);
    }

    @Override
    public void setDelegate(final S newDelegate) {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }

    @Override
    public void clearDelegate() {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }
}
