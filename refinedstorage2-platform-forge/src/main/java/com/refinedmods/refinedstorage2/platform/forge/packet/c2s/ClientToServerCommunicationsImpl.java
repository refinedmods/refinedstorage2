package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyType;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.common.packet.ClientToServerCommunications;
import com.refinedmods.refinedstorage2.platform.forge.packet.NetworkManager;

import java.util.UUID;

public class ClientToServerCommunicationsImpl implements ClientToServerCommunications {
    private final NetworkManager networkManager;

    public ClientToServerCommunicationsImpl(final NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    @Override
    public void sendGridItemExtract(final ItemResource itemResource, final GridExtractMode mode, final boolean cursor) {
        networkManager.send(new GridExtractPacket(mode, cursor, itemResource));
    }

    @Override
    public void sendGridFluidExtract(final FluidResource fluidResource,
                                     final GridExtractMode mode,
                                     final boolean cursor) {
        networkManager.send(new GridExtractPacket(mode, cursor, fluidResource));
    }

    @Override
    public void sendGridInsert(final GridInsertMode mode) {
        networkManager.send(new GridInsertPacket(mode == GridInsertMode.SINGLE_RESOURCE));
    }

    @Override
    public void sendGridScroll(final ItemResource itemResource, final GridScrollMode mode, final int slotIndex) {
        networkManager.send(new GridScrollPacket(itemResource, mode, slotIndex));
    }

    @Override
    public <T> void sendPropertyChange(final PropertyType<T> type, final T value) {
        networkManager.send(new PropertyChangePacket(type.id(), type.serializer().apply(value)));
    }

    @Override
    public void sendResourceTypeChange(final ResourceType type) {
        PlatformApi.INSTANCE.getResourceTypeRegistry()
            .getId(type)
            .ifPresent(id -> networkManager.send(new ResourceTypeChangePacket(id)));
    }

    @Override
    public void sendStorageInfoRequest(final UUID storageId) {
        networkManager.send(new StorageInfoRequestPacket(storageId));
    }

    @Override
    public void sendResourceFilterSlotAmountChange(final int slotIndex, final long amount) {
        networkManager.send(new ResourceFilterSlotAmountChangePacket(slotIndex, amount));
    }
}
