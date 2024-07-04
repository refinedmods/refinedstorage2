package com.refinedmods.refinedstorage.platform.common.support.packet.c2s;

import com.refinedmods.refinedstorage.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage.platform.api.security.PlatformPermission;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.PropertyType;
import com.refinedmods.refinedstorage.platform.common.support.resource.ItemResource;

import java.util.List;
import java.util.UUID;

public final class C2SPackets {
    private C2SPackets() {
    }

    public static void sendGridExtract(final PlatformResourceKey resource,
                                       final GridExtractMode mode,
                                       final boolean cursor) {
        Platform.INSTANCE.sendPacketToServer(new GridExtractPacket(resource, mode, cursor));
    }

    public static void sendGridScroll(final PlatformResourceKey resource,
                                      final GridScrollMode mode,
                                      final int slotIndex) {
        Platform.INSTANCE.sendPacketToServer(new GridScrollPacket(resource, mode, slotIndex));
    }

    public static void sendGridInsert(final GridInsertMode mode, final boolean tryAlternatives) {
        Platform.INSTANCE.sendPacketToServer(new GridInsertPacket(mode, tryAlternatives));
    }

    public static void sendCraftingGridClear(final boolean toPlayerInventory) {
        Platform.INSTANCE.sendPacketToServer(new CraftingGridClearPacket(toPlayerInventory));
    }

    public static void sendCraftingGridRecipeTransfer(final List<List<ItemResource>> recipe) {
        Platform.INSTANCE.sendPacketToServer(new CraftingGridRecipeTransferPacket(recipe));
    }

    public static <T> void sendPropertyChange(final PropertyType<T> type, final T value) {
        Platform.INSTANCE.sendPacketToServer(new PropertyChangePacket(type.id(), type.serializer().apply(value)));
    }

    public static void sendStorageInfoRequest(final UUID storageId) {
        Platform.INSTANCE.sendPacketToServer(new StorageInfoRequestPacket(storageId));
    }

    public static void sendResourceSlotChange(final int slotIndex, final boolean tryAlternatives) {
        Platform.INSTANCE.sendPacketToServer(new ResourceSlotChangePacket(slotIndex, tryAlternatives));
    }

    public static void sendResourceFilterSlotChange(final PlatformResourceKey resource, final int slotIndex) {
        Platform.INSTANCE.sendPacketToServer(new ResourceFilterSlotChangePacket(slotIndex, resource));
    }

    public static void sendResourceSlotAmountChange(final int slotIndex, final long amount) {
        Platform.INSTANCE.sendPacketToServer(new ResourceSlotAmountChangePacket(slotIndex, amount));
    }

    public static void sendSingleAmountChange(final double amount) {
        Platform.INSTANCE.sendPacketToServer(new SingleAmountChangePacket(amount));
    }

    public static void sendUseNetworkBoundItem(final SlotReference slotReference) {
        Platform.INSTANCE.sendPacketToServer(new UseNetworkBoundItemPacket(slotReference));
    }

    public static void sendSecurityCardPermission(final PlatformPermission permission, final boolean allowed) {
        Platform.INSTANCE.sendPacketToServer(new SecurityCardPermissionPacket(permission, allowed));
    }

    public static void sendSecurityCardResetPermission(final PlatformPermission permission) {
        Platform.INSTANCE.sendPacketToServer(new SecurityCardResetPermissionPacket(permission));
    }

    public static void sendSecurityCardBoundPlayer(final UUID playerId) {
        Platform.INSTANCE.sendPacketToServer(new SecurityCardBoundPlayerPacket(playerId));
    }
}
