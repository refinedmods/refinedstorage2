package com.refinedmods.refinedstorage2.api.resource.list;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.UUID;

public record ResourceListOperationResult<T>(ResourceAmount<T> resourceAmount, long change, UUID id,
                                             boolean available) {
}
