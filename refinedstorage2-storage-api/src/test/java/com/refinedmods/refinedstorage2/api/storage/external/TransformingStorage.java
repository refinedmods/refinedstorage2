package com.refinedmods.refinedstorage2.api.storage.external;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.AbstractProxyStorage;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;

import static com.refinedmods.refinedstorage2.api.storage.external.ExternalTestResource.A_ALTERNATIVE;
import static com.refinedmods.refinedstorage2.api.storage.external.ExternalTestResource.A_TRANSFORMED;

class TransformingStorage extends AbstractProxyStorage {
    TransformingStorage() {
        super(new InMemoryStorageImpl());
    }

    private ResourceKey transform(final ResourceKey resource) {
        if (resource == ExternalTestResource.A) {
            return A_TRANSFORMED;
        } else if (resource == ExternalTestResource.B) {
            return ExternalTestResource.B_TRANSFORMED;
        }
        return resource;
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return super.insert(transform(resource), amount, action, actor);
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        final long extracted = super.extract(resource, amount, action, actor);
        if (resource == A_TRANSFORMED) {
            super.extract(A_ALTERNATIVE, amount / 2, action, actor);
        }
        return extracted;
    }
}
