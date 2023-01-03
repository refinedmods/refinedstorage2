package com.refinedmods.refinedstorage2.api.network.impl.node.diskdrive;

import com.refinedmods.refinedstorage2.api.storage.TypedStorage;

import java.util.Optional;

@FunctionalInterface
public interface DiskDriveProvider {
    Optional<TypedStorage<?>> resolve(int slot);
}
