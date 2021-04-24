package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.storage.StorageChannel;

import java.util.Set;
import java.util.UUID;

public interface Network {
    UUID getId();

    Set<NetworkNodeReference> getNodeReferences();

    void invalidateStorageChannelSources();

    StorageChannel<Rs2ItemStack> getItemStorageChannel();
}
