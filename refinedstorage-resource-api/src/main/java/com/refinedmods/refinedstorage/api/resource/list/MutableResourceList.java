package com.refinedmods.refinedstorage.api.resource.list;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

/**
 * Mutable variant of {@link ResourceList}.
 * Implementation can be found in {@link MutableResourceListImpl}.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public interface MutableResourceList extends ResourceList {
    /**
     * Adds a given resource to the list.
     *
     * @param resource the resource, may not be null
     * @param amount   the amount, must be larger than 0
     * @return the result of the operation
     */
    OperationResult add(ResourceKey resource, long amount);

    /**
     * Adds a given resource to the list.
     * Shorthand for {@link #add(ResourceKey, long)}.
     *
     * @param resourceAmount the resource and the amount
     * @return the result of the operation
     */
    default OperationResult add(final ResourceAmount resourceAmount) {
        return add(resourceAmount.resource(), resourceAmount.amount());
    }

    /**
     * Removes as much of a certain amount of a resource in the list.
     * If the amount reaches 0 due to this removal, the resource is removed from the list.
     *
     * @param resource the resource, may not be null
     * @param amount   the amount, must be larger than 0
     * @return a result if the removal operation was successful, otherwise null
     */
    @Nullable
    OperationResult remove(ResourceKey resource, long amount);

    /**
     * Removes as much of a certain amount of a resource in the list.
     * If the amount reaches 0 due to this removal, the resource is removed from the list.
     * Shorthand for {@link #remove(ResourceKey, long)}.
     *
     * @param resourceAmount the resource and the amount
     * @return a result if the removal operation was successful, otherwise null
     */
    @Nullable
    default OperationResult remove(final ResourceAmount resourceAmount) {
        return remove(resourceAmount.resource(), resourceAmount.amount());
    }

    /**
     * Clears the list.
     */
    void clear();

    /**
     * Copies the list.
     */
    MutableResourceList copy();

    /**
     * Represents the result of an operation in a {@link MutableResourceList}.
     *
     * @param resource  the resource affected by the operation
     * @param amount    the current amount in the list
     * @param change    the delta caused by the operation
     * @param available whether this resource is still available in the list, or if it was removed
     */
    @API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
    record OperationResult(ResourceKey resource, long amount, long change, boolean available) {
    }
}
