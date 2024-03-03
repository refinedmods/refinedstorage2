package com.refinedmods.refinedstorage2.api.network.impl.node.importer;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterSource;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class FakeImporterSource implements ImporterSource {
    private final List<ResourceKey> resources;
    private final InMemoryStorageImpl storage = new InMemoryStorageImpl();

    public FakeImporterSource(final ResourceKey... resources) {
        this.resources = Arrays.stream(resources).toList();
    }

    public FakeImporterSource add(final ResourceKey resource, final long amount) {
        storage.insert(resource, amount, Action.EXECUTE, EmptyActor.INSTANCE);
        return this;
    }

    @Override
    public Iterator<ResourceKey> getResources() {
        return resources.iterator();
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        // Extract a maximum of 5 to ensure that we try to extract multiple times from different slots.
        return storage.extract(resource, Math.min(amount, 5), action, actor);
    }

    public Collection<ResourceAmount> getAll() {
        return storage.getAll();
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        return storage.insert(resource, amount, action, actor);
    }
}
