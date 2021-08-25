package com.refinedmods.refinedstorage2.api.stack.list;

import java.util.UUID;

public record StackListResult<T>(T stack, long change, UUID id, boolean available) {
}
