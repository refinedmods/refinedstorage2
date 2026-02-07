package com.refinedmods.refinedstorage.api.storage.root;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.listenable.ResourceListListener;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public interface RootStorageListener extends ResourceListListener {
    /**
     * Called before a resource is inserted into the {@link RootStorage}.
     * Use this to intercept resources before they get inserted.
     *
     * @param resource the resource
     * @param amount   the amount
     * @return the amount intercepted that will not be passed to other before insert listeners and the storage
     */
    default long beforeInsert(final ResourceKey resource, final long amount) {
        return 0;
    }

    /**
     * Called after a resource has been inserted into the {@link RootStorage}.
     * Use this to detect when something has been inserted.
     *
     * @param resource the resource
     * @param amount   the amount
     * @return the amount reserved that will not be passed to other after insert listeners
     */
    default long afterInsert(final ResourceKey resource, final long amount) {
        return 0;
    }
}
