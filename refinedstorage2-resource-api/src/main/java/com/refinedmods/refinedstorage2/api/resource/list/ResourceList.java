package com.refinedmods.refinedstorage2.api.resource.list;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Collection;
import java.util.Optional;

import org.apiguardian.api.API;

/**
 * Represents a list of a resource of an arbitrary type.
 * A basic implementation of this class can be found in {@link ResourceListImpl}.
 *
 * @param <T> the type of resource
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public interface ResourceList<T> {
    /**
     * Adds a given resource to the list.
     *
     * @param resource the resource, may not be null
     * @param amount   the amount, must be larger than 0
     * @return the result of the operation
     */
    ResourceListOperationResult<T> add(T resource, long amount);

    /**
     * Adds a given resource to the list.
     * Shorthand for {@link #add(Object, long)}.
     *
     * @param resourceAmount the resource and the amount
     * @return the result of the operation
     */
    default ResourceListOperationResult<T> add(ResourceAmount<T> resourceAmount) {
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
    Optional<ResourceListOperationResult<T>> remove(T resource, long amount);

    /**
     * Removes an amount of a certain resource in the list.
     * If the amount reaches 0 due to this removal, the resource is removed from the list.
     * Shorthand for {@link #remove(Object, long)}.
     *
     * @param resourceAmount the resource and the amount
     * @return a result if the removal operation was successful, otherwise an empty {@link Optional}
     */
    default Optional<ResourceListOperationResult<T>> remove(ResourceAmount<T> resourceAmount) {
        return remove(resourceAmount.getResource(), resourceAmount.getAmount());
    }

    /**
     * Retrieves the resource and its amount from the list, identified by resource.
     *
     * @param resource the resource
     * @return the resource amount if it's present in the list, otherwise an empty {@link Optional}
     */
    Optional<ResourceAmount<T>> get(T resource);

    /**
     * Retrieves all resources and their amounts from the list.
     *
     * @return a list of resource amounts
     */
    Collection<ResourceAmount<T>> getAll();

    /**
     * Clears the list.
     */
    void clear();
}
