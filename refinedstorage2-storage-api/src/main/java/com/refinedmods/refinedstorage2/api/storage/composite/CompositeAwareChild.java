package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.Storage;

import org.apiguardian.api.API;

/**
 * Implement this on storages that need to be aware of the fact that they are contained in a {@link CompositeStorage}.
 * Typically, this is needed so that storages that dynamically modify their underlying storage sources, can propagate
 * the changes to the parent composite list.
 * Another reason to be aware of the parent composite is to be able to override the amount modified into the composite
 * cache list.
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

    /**
     * Inserts a resource into a storage.
     *
     * @param resource the resource, may not be null
     * @param amount   the amount, must be larger than 0
     * @param action   the mode of insertion
     * @param actor    the source
     * @return the amount inserted
     */
    Amount compositeInsert(ResourceKey resource, long amount, Action action, Actor actor);

    /**
     * Extracts a resource from a storage.
     *
     * @param resource the resource, may not be null
     * @param amount   the amount, must be larger than 0
     * @param action   the mode of extraction
     * @param actor    the source
     * @return the amount extracted
     */
    Amount compositeExtract(ResourceKey resource, long amount, Action action, Actor actor);

    /**
     * @param amount        the amount
     * @param amountForList the amount to be modified in the parent composite cache list
     */
    record Amount(long amount, long amountForList) {
        public static final Amount ZERO = new Amount(0, 0);

        public Amount withoutNotifyingList() {
            return new Amount(amount, 0);
        }
    }
}
