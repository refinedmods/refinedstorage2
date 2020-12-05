package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.storage.StorageChannel;
import net.minecraft.item.ItemStack;

import java.util.Set;
import java.util.UUID;

public interface Network {
    UUID getId();

    Set<NetworkNodeReference> getNodeReferences();

    void onNodesChanged();

    StorageChannel<ItemStack> getItemStorageChannel();
}
