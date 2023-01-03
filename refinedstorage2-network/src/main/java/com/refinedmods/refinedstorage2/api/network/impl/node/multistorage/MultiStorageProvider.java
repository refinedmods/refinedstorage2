package com.refinedmods.refinedstorage2.api.network.impl.node.multistorage;

import com.refinedmods.refinedstorage2.api.storage.TypedStorage;

import java.util.Optional;

@FunctionalInterface
public interface MultiStorageProvider {
    Optional<TypedStorage<?>> resolve(int index);
}
