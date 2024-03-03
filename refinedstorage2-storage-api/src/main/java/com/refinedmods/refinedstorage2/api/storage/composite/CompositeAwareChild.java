package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.storage.Storage;

import org.apiguardian.api.API;

/**
 * Implement this on storages that need to be aware of the fact that they are contained in a {@link CompositeStorage}.
 * Typically, this is needed so that storages that dynamically modify their underlying storage sources, can propagate
 * the changes to the parent composite list.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public interface CompositeAwareChild extends Storage {
    /**
     * Called by a {@link CompositeStorage} when this {@link CompositeAwareChild} is added into the composite storage.
     *
     * @param parentComposite the composite storage that this {@link CompositeAwareChild} is contained in
     */
    void onAddedIntoComposite(ParentComposite parentComposite);

    /**
     * Called by a {@link CompositeStorage} when this {@link CompositeAwareChild} is removed from the composite storage.
     *
     * @param parentComposite the composite storage that this {@link CompositeAwareChild} is/was contained in
     */
    void onRemovedFromComposite(ParentComposite parentComposite);
}
