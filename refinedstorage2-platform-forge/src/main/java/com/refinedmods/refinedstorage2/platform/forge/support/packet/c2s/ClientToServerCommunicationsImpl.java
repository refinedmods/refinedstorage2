package com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s;

import com.refinedmods.refinedstorage2.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.support.ClientToServerCommunications;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.PropertyType;

import java.util.List;
import java.util.UUID;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClientToServerCommunicationsImpl implements ClientToServerCommunications {
    private void sendPacket(final CustomPacketPayload packet) {
        PacketDistributor.SERVER.noArg().send(packet);
    }

    @Override
    public void sendGridExtract(final PlatformStorageChannelType storageChannelType,
                                final ResourceKey resource,
                                final GridExtractMode mode,
                                final boolean cursor) {
        PlatformApi.INSTANCE.getStorageChannelTypeRegistry().getId(storageChannelType)
            .ifPresent(id -> sendPacket(new GridExtractPacket(
                storageChannelType,
                id,
                resource,
                mode,
                cursor
            )));
    }

    @Override
    public void sendGridScroll(final PlatformStorageChannelType storageChannelType,
                               final ResourceKey resource,
                               final GridScrollMode mode,
                               final int slotIndex) {
        PlatformApi.INSTANCE.getStorageChannelTypeRegistry()
            .getId(storageChannelType)
            .ifPresent(id -> sendPacket(new GridScrollPacket(
                storageChannelType,
                id,
                resource,
                mode,
                slotIndex
            )));
    }

    @Override
    public void sendGridInsert(final GridInsertMode mode, final boolean tryAlternatives) {
        sendPacket(new GridInsertPacket(mode == GridInsertMode.SINGLE_RESOURCE, tryAlternatives));
    }

    @Override
    public void sendCraftingGridClear(final boolean toPlayerInventory) {
        sendPacket(new CraftingGridClearPacket(toPlayerInventory));
    }

    @Override
    public void sendCraftingGridRecipeTransfer(final List<List<ItemResource>> recipe) {
        sendPacket(new CraftingGridRecipeTransferPacket(recipe));
    }

    @Override
    public <T> void sendPropertyChange(final PropertyType<T> type, final T value) {
        sendPacket(new PropertyChangePacket(type.id(), type.serializer().apply(value)));
    }

    @Override
    public void sendStorageInfoRequest(final UUID storageId) {
        sendPacket(new StorageInfoRequestPacket(storageId));
    }

    @Override
    public void sendResourceSlotChange(final int slotIndex, final boolean tryAlternatives) {
        sendPacket(new ResourceSlotChangePacket(slotIndex, tryAlternatives));
    }

    @Override
    public void sendResourceFilterSlotChange(final PlatformStorageChannelType storageChannelType,
                                             final ResourceKey resource,
                                             final int slotIndex) {
        PlatformApi.INSTANCE.getStorageChannelTypeRegistry().getId(storageChannelType).ifPresent(
            id -> sendPacket(new ResourceFilterSlotChangePacket(
                slotIndex,
                resource,
                storageChannelType,
                id
            ))
        );
    }

    @Override
    public void sendResourceSlotAmountChange(final int slotIndex, final long amount) {
        sendPacket(new ResourceSlotAmountChangePacket(slotIndex, amount));
    }

    @Override
    public void sendSingleAmountChange(final double amount) {
        sendPacket(new SingleAmountChangePacket(amount));
    }

    @Override
    public void sendUseNetworkBoundItem(final SlotReference slotReference) {
        sendPacket(new UseNetworkBoundItemPacket(slotReference));
    }
}
