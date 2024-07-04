package com.refinedmods.refinedstorage.platform.common.support;

import com.refinedmods.refinedstorage.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage.platform.api.security.PlatformPermission;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.PropertyType;
import com.refinedmods.refinedstorage.platform.common.support.resource.ItemResource;

import java.util.List;
import java.util.UUID;

public interface ClientToServerCommunications {
    void sendGridExtract(PlatformResourceKey resource, GridExtractMode mode, boolean cursor);

    void sendGridScroll(PlatformResourceKey resource, GridScrollMode mode, int slotIndex);

    void sendGridInsert(GridInsertMode mode, boolean tryAlternatives);

    void sendCraftingGridClear(boolean toPlayerInventory);

    void sendCraftingGridRecipeTransfer(List<List<ItemResource>> recipe);

    <T> void sendPropertyChange(PropertyType<T> type, T value);

    void sendStorageInfoRequest(UUID storageId);

    void sendResourceSlotChange(int slotIndex, boolean tryAlternatives);

    void sendResourceFilterSlotChange(PlatformResourceKey resource, int slotIndex);

    void sendResourceSlotAmountChange(int slotIndex, long amount);

    void sendSingleAmountChange(double amount);

    void sendUseNetworkBoundItem(SlotReference slotReference);

    void sendSecurityCardPermission(PlatformPermission permission, boolean allowed);

    void sendSecurityCardResetPermission(PlatformPermission permission);

    void sendSecurityCardBoundPlayer(UUID playerId);
}
