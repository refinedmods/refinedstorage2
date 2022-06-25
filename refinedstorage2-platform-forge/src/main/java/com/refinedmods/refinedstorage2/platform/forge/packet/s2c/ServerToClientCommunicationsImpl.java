package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.packet.ServerToClientCommunications;
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
        networkManager.send(player, new GridActivePacket(active));
    }

    @Override
    public void sendGridFluidUpdate(ServerPlayer player, FluidResource fluidResource, long change, TrackedResource trackedResource) {
        networkManager.send(player, new GridFluidUpdatePacket(fluidResource, change, trackedResource));
    }

    @Override
    public void sendGridItemUpdate(ServerPlayer player, ItemResource itemResource, long change, TrackedResource trackedResource) {
        networkManager.send(player, new GridItemUpdatePacket(itemResource, change, trackedResource));
    }

    @Override
    public void sendResourceFilterSlotUpdate(ServerPlayer player, ResourceFilterContainer resourceFilterContainer, int slotIndex) {
        networkManager.send(player, new ResourceFilterSlotUpdatePacket(slotIndex, resourceFilterContainer));
    }

    @Override
    public void sendStorageInfoResponse(ServerPlayer player, UUID id, StorageInfo storageInfo) {
        networkManager.send(player, new StorageInfoResponsePacket(id, storageInfo.stored(), storageInfo.capacity()));
    }
}
