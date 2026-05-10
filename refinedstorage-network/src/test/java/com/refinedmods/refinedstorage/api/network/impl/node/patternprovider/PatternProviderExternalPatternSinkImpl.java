package com.refinedmods.refinedstorage.api.network.impl.node.patternprovider;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProviderExternalPatternSink;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;

import java.util.Collection;

public class PatternProviderExternalPatternSinkImpl implements PatternProviderExternalPatternSink {
    private final Storage storage = new StorageImpl();
    private boolean locked;

    @Override
    public ExternalPatternSink.Result accept(final Collection<ResourceAmount> resources, final Action action) {
        if (locked) {
            return ExternalPatternSink.Result.LOCKED;
        }
        if (action == Action.EXECUTE) {
            resources.forEach(resource ->
                storage.insert(resource.resource(), resource.amount(), Action.EXECUTE, Actor.EMPTY));
        }
        return ExternalPatternSink.Result.ACCEPTED;
    }

    public void setLocked(final boolean locked) {
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }

    public Collection<ResourceAmount> getAll() {
        return storage.getAll();
    }
}
