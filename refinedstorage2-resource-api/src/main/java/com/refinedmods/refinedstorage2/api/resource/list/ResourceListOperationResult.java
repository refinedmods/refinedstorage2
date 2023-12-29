package com.refinedmods.refinedstorage2.api.resource.list;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import org.apiguardian.api.API;

/**
 * Represents the result of an operation in a {@link ResourceList}.
 *
 * @param <T>            the type of resource
 * @param resourceAmount the current resource amount in the list
 * @param change         the delta caused by the operation
 * @param available      whether this resource is still available in the list, or if it was removed
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public record ResourceListOperationResult<T>(ResourceAmount<T> resourceAmount, long change, boolean available) {
}
