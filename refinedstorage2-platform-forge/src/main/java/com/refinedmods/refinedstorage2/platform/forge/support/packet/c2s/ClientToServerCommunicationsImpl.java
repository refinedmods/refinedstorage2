package com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s;

import com.refinedmods.refinedstorage2.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage2.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.support.ClientToServerCommunications;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.PropertyType;
import com.refinedmods.refinedstorage2.platform.common.support.resource.ItemResource;

import java.util.List;
import java.util.UUID;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClientToServerCommunicationsImpl implements ClientToServerCommunications {
    private void sendPacket(final CustomPacketPayload packet) {
        PacketDistributor.SERVER.noArg().send(packet);
    }

    @Override
    public void sendGridExtract(final PlatformResourceKey resource,
                                final GridExtractMode mode,
                                final boolean cursor) {
        final ResourceType resourceType = resource.getResourceType();
        PlatformApi.INSTANCE.getResourceTypeRegistry().getId(resourceType)
            .ifPresent(id -> sendPacket(new GridExtractPacket(
                resourceType,
                id,
                resource,
                mode,
                cursor
            )));
    }

    @Override
    public void sendGridScroll(final PlatformResourceKey resource,
                               final GridScrollMode mode,
                               final int slotIndex) {
        final ResourceType resourceType = resource.getResourceType();
        PlatformApi.INSTANCE.getResourceTypeRegistry()
            .getId(resourceType)
            .ifPresent(id -> sendPacket(new GridScrollPacket(
                resourceType,
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
    public void sendResourceFilterSlotChange(final PlatformResourceKey resource, final int slotIndex) {
        final ResourceType resourceType = resource.getResourceType();
        PlatformApi.INSTANCE.getResourceTypeRegistry().getId(resourceType).ifPresent(
            id -> sendPacket(new ResourceFilterSlotChangePacket(
                slotIndex,
                resource,
                resourceType,
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
