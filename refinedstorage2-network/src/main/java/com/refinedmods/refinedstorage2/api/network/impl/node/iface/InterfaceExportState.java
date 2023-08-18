package com.refinedmods.refinedstorage2.api.network.impl.node.iface;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.Collection;
import javax.annotation.Nullable;

public interface InterfaceExportState {
    int getSlots();

    <T> Collection<T> expandExportCandidates(StorageChannel<T> storageChannel, T resource);

    <A, B> boolean isExportedResourceValid(ResourceTemplate<A> want,
                                           ResourceTemplate<B> got);

    @Nullable
    ResourceTemplate<?> getRequestedResource(int slotIndex);

    long getRequestedAmount(int slotIndex);

    @Nullable
    ResourceTemplate<?> getExportedResource(int slotIndex);

    long getExportedAmount(int slotIndex);

    <T> void setExportSlot(int slotIndex, ResourceTemplate<T> resource, long amount);

    void shrinkExportedAmount(int slotIndex, long amount);

    void growExportedAmount(int slotIndex, long amount);

    <T> long insert(StorageChannelType<T> storageChannelType, T resource, long amount, Action action);

    <T> long extract(T resource, long amount, Action action);
}
