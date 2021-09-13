package com.refinedmods.refinedstorage2.api.stack.list;

import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;

import java.util.UUID;

// TODO: Rename to ResourceListOperationResult
public record StackListResult<T>(ResourceAmount<T> resourceAmount, long change, UUID id, boolean available) {
}
