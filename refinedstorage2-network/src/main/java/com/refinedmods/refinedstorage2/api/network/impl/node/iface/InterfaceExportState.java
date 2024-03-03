package com.refinedmods.refinedstorage2.api.network.impl.node.iface;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Collection;
import javax.annotation.Nullable;

public interface InterfaceExportState {
    int getSlots();

    Collection<ResourceKey> expandExportCandidates(StorageChannel storageChannel, ResourceKey resource);

    boolean isExportedResourceValid(ResourceTemplate want,
                                    ResourceTemplate got);

    @Nullable
    ResourceTemplate getRequestedResource(int slotIndex);

    long getRequestedAmount(int slotIndex);

    @Nullable
    ResourceTemplate getExportedResource(int slotIndex);

    long getExportedAmount(int slotIndex);

    void setExportSlot(int slotIndex, ResourceTemplate resource, long amount);

    void shrinkExportedAmount(int slotIndex, long amount);

    void growExportedAmount(int slotIndex, long amount);

    long insert(StorageChannelType storageChannelType, ResourceKey resource, long amount, Action action);

    long extract(ResourceKey resource, long amount, Action action);
}
