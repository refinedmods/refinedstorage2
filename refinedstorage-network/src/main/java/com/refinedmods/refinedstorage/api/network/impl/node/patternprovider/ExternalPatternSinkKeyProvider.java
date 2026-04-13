package com.refinedmods.refinedstorage.api.network.impl.node.patternprovider;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkKey;

import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface ExternalPatternSinkKeyProvider {
    @Nullable
    ExternalPatternSinkKey getKey();
}
