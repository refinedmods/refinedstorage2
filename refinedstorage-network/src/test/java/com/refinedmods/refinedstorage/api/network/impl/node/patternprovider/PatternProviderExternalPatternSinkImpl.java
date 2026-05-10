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
    private final boolean lockAfterAccept;
    private boolean locked;

    public PatternProviderExternalPatternSinkImpl() {
        this(false);
    }

    private PatternProviderExternalPatternSinkImpl(final boolean lockAfterAccept) {
        this.lockAfterAccept = lockAfterAccept;
    }

    @Override
    public ExternalPatternSink.Result accept(final Collection<ResourceAmount> resources, final Action action) {
        if (locked) {
            return ExternalPatternSink.Result.LOCKED;
        }
        if (action == Action.EXECUTE) {
            resources.forEach(resource ->
                storage.insert(resource.resource(), resource.amount(), Action.EXECUTE, Actor.EMPTY));
            if (lockAfterAccept) {
                locked = true;
            }
        }
        return ExternalPatternSink.Result.ACCEPTED;
    }

    public void unlock() {
        locked = false;
    }

    public boolean isLocked() {
        return locked;
    }

    public Collection<ResourceAmount> getAll() {
        return storage.getAll();
    }

    public static PatternProviderExternalPatternSinkImpl lockAfterAccept() {
        return new PatternProviderExternalPatternSinkImpl(true);
    }
}
