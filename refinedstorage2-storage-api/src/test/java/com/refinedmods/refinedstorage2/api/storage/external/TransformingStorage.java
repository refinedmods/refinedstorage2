package com.refinedmods.refinedstorage2.api.storage.external;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.AbstractProxyStorage;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;

class TransformingStorage extends AbstractProxyStorage<String> {
    TransformingStorage() {
        super(new InMemoryStorageImpl<>());
    }

    @Override
    public long insert(final String resource, final long amount, final Action action, final Actor actor) {
        return super.insert(resource + "!", amount, action, actor);
    }

    @Override
    public long extract(final String resource, final long amount, final Action action, final Actor actor) {
        final long extracted = super.extract(resource, amount, action, actor);
        super.extract(resource.replace("!", "") + "2!", amount / 2, action, actor);
        return extracted;
    }
}
