package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.platform.abstractions.packet.ServerToClientCommunications;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.forge.packet.NetworkManager;

import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;

public class ServerToClientCommunicationsImpl implements ServerToClientCommunications {
    private final NetworkManager networkManager;

    public ServerToClientCommunicationsImpl(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    @Override
    public void sendControllerEnergy(ServerPlayer player, long stored, long capacity) {
        networkManager.send(player, new ControllerEnergyPacket(stored, capacity));
    }

    @Override
    public void sendGridActiveness(ServerPlayer player, boolean active) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendGridFluidUpdate(ServerPlayer player, FluidResource fluidResource, long change, StorageTracker.Entry trackerEntry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendGridItemUpdate(ServerPlayer player, ItemResource itemResource, long change, StorageTracker.Entry trackerEntry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendResourceFilterSlotUpdate(ServerPlayer player, ResourceFilterContainer resourceFilterContainer, int slotIndex) {
        networkManager.send(player, new ResourceFilterSlotUpdatePacket(slotIndex));
    }

    @Override
    public void sendStorageInfoResponse(ServerPlayer player, UUID id, StorageInfo storageInfo) {
        networkManager.send(player, new StorageInfoResponsePacket(id, storageInfo.stored(), storageInfo.capacity()));
    }
}
