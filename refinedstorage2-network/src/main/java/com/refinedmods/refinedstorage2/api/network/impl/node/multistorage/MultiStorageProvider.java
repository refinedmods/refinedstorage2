package com.refinedmods.refinedstorage2.api.network.impl.node.multistorage;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.TypedStorage;

import java.util.Optional;

@FunctionalInterface
public interface MultiStorageProvider {
    <T> Optional<TypedStorage<T, Storage<T>>> resolve(int index);
}
