package com.refinedmods.refinedstorage2.api.network.impl.node.diskdrive;

import com.refinedmods.refinedstorage2.api.storage.TypedStorage;

import java.util.Optional;

// TODO: Rename to DiskDriveProvider
@FunctionalInterface
public interface StorageDiskProvider {
    Optional<TypedStorage<?>> resolve(int slot);
}
