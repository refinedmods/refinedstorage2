package com.refinedmods.refinedstorage2.platform.common.packet;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
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

    void sendResourceFilterSlotChange(int slotIndex, boolean tryAlternatives);

    void sendResourceFilterSlotAmountChange(int slotIndex, long amount);

    void sendDetectorAmountChange(long amount);
}
