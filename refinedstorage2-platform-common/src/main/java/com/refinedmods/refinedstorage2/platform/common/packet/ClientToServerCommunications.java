package com.refinedmods.refinedstorage2.platform.common.packet;

import com.refinedmods.refinedstorage2.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.item.SlotReference;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyType;

import java.util.List;
import java.util.UUID;

public interface ClientToServerCommunications {
    <T> void sendGridExtract(PlatformStorageChannelType<T> storageChannelType,
                             T resource,
                             GridExtractMode mode,
                             boolean cursor);

    <T> void sendGridScroll(PlatformStorageChannelType<T> storageChannelType,
                            T resource,
                            GridScrollMode mode,
                            int slotIndex);

    void sendGridInsert(GridInsertMode mode, boolean tryAlternatives);

    void sendCraftingGridClear(boolean toPlayerInventory);

    void sendCraftingGridRecipeTransfer(List<List<ItemResource>> recipe);

    <T> void sendPropertyChange(PropertyType<T> type, T value);

    void sendStorageInfoRequest(UUID storageId);

    void sendResourceSlotChange(int slotIndex, boolean tryAlternatives);

    <T> void sendResourceFilterSlotChange(PlatformStorageChannelType<T> storageChannelType, T resource, int slotIndex);

    void sendResourceSlotAmountChange(int slotIndex, long amount);

    void sendSingleAmountChange(double amount);

    void sendUseNetworkBoundItem(SlotReference slotReference);
}
