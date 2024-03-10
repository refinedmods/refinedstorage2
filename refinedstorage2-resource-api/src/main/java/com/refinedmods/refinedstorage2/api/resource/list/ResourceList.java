package com.refinedmods.refinedstorage2.api.resource.list;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;

import java.util.Collection;
import java.util.Optional;

import org.apiguardian.api.API;

/**
 * Represents a list of a resource of an arbitrary type.
 * A basic implementation of this class can be found in {@link ResourceListImpl}.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public interface ResourceList {
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
    default OperationResult add(ResourceAmount resourceAmount) {
        return add(resourceAmount.getResource(), resourceAmount.getAmount());
    }

    /**
     * Removes an amount of a certain resource in the list.
     * If the amount reaches 0 due to this removal, the resource is removed from the list.
     *
     * @param resource the resource, may not be null
     * @param amount   the amount, must be larger than 0
     * @return a result if the removal operation was successful, otherwise an empty {@link Optional}
     */
    Optional<OperationResult> remove(ResourceKey resource, long amount);

    /**
     * Removes an amount of a certain resource in the list.
     * If the amount reaches 0 due to this removal, the resource is removed from the list.
     * Shorthand for {@link #remove(ResourceKey, long)}.
     *
     * @param resourceAmount the resource and the amount
     * @return a result if the removal operation was successful, otherwise an empty {@link Optional}
     */
    default Optional<OperationResult> remove(ResourceAmount resourceAmount) {
        return remove(resourceAmount.getResource(), resourceAmount.getAmount());
    }

    /**
     * Retrieves the resource and its amount from the list, identified by resource.
     *
     * @param resource the resource
     * @return the resource amount if it's present in the list, otherwise an empty {@link Optional}
     */
    Optional<ResourceAmount> get(ResourceKey resource);

    /**
     * Retrieves all resources and their amounts from the list.
     *
     * @return a list of resource amounts
     */
    Collection<ResourceAmount> getAll();

    /**
     * Clears the list.
     */
    void clear();

    /**
     * Represents the result of an operation in a {@link ResourceList}.
     *
     * @param resourceAmount the current resource amount in the list
     * @param change         the delta caused by the operation
     * @param available      whether this resource is still available in the list, or if it was removed
     */
    @API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
    record OperationResult(ResourceAmount resourceAmount, long change, boolean available) {
    }
}
