package com.refinedmods.refinedstorage2.api.network.impl.node.multistorage;

import com.refinedmods.refinedstorage2.api.storage.Storage;

import java.util.Optional;

@FunctionalInterface
public interface MultiStorageProvider {
    Optional<Storage> resolve(int index);
}
