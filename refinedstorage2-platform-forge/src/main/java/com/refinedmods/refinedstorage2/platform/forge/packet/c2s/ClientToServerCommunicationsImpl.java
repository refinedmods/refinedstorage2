package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.abstractions.packet.ClientToServerCommunications;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.forge.packet.NetworkManager;

import java.util.UUID;

public class ClientToServerCommunicationsImpl implements ClientToServerCommunications {
    private final NetworkManager networkManager;

    public ClientToServerCommunicationsImpl(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    @Override
    public void sendGridItemExtract(ItemResource itemResource, GridExtractMode mode, boolean cursor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendGridFluidExtract(FluidResource fluidResource, GridExtractMode mode, boolean cursor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendGridInsert(GridInsertMode mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendGridScroll(ItemResource itemResource, GridScrollMode mode, int slot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendPropertyChange(int id, int value) {
        networkManager.send(new PropertyChangePacket(id, value));
    }

    @Override
    public void sendResourceTypeChange(ResourceType<?> type) {
        networkManager.send(new ResourceTypeChangePacket(type.getId()));
    }

    @Override
    public void sendStorageInfoRequest(UUID storageId) {
        networkManager.send(new StorageInfoRequestPacket(storageId));
    }
}
