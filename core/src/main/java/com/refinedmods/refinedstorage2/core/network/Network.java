package com.refinedmods.refinedstorage2.core.network;

import java.util.Set;
import java.util.UUID;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.storage.StorageChannel;
import net.minecraft.item.ItemStack;

public interface Network {
    UUID getId();

    Set<NetworkNodeReference> getNodeReferences();

    void invalidateStorageChannelSources();

    StorageChannel<ItemStack> getItemStorageChannel();
}
