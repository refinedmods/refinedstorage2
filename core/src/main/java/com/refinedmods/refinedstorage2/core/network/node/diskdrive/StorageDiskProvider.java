package com.refinedmods.refinedstorage2.core.network.node.diskdrive;

import java.util.Optional;
import java.util.UUID;

public interface StorageDiskProvider {
    Optional<UUID> getDiskId(int slot);
}
