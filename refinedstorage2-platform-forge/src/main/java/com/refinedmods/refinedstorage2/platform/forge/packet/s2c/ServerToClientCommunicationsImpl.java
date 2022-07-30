package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.packet.ServerToClientCommunications;
import com.refinedmods.refinedstorage2.platform.forge.packet.NetworkManager;

import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;

public class ServerToClientCommunicationsImpl implements ServerToClientCommunications {
    private final NetworkManager networkManager;

    public ServerToClientCommunicationsImpl(final NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    @Override
    public void sendControllerEnergyInfo(final ServerPlayer player, final long stored, final long capacity) {
        networkManager.send(player, new ControllerEnergyInfoPacket(stored, capacity));
    }

    @Override
    public void sendGridActiveness(final ServerPlayer player, final boolean active) {
        networkManager.send(player, new GridActivePacket(active));
    }

    @Override
    public void sendGridFluidUpdate(final ServerPlayer player,
                                    final FluidResource fluidResource,
                                    final long change,
                                    @Nullable final TrackedResource trackedResource) {
        networkManager.send(player, new GridFluidUpdatePacket(fluidResource, change, trackedResource));
    }

    @Override
    public void sendGridItemUpdate(final ServerPlayer player,
                                   final ItemResource itemResource,
                                   final long change,
                                   @Nullable final TrackedResource trackedResource) {
        networkManager.send(player, new GridItemUpdatePacket(itemResource, change, trackedResource));
    }

    @Override
    public void sendResourceFilterSlotUpdate(final ServerPlayer player,
                                             final ResourceFilterContainer resourceFilterContainer,
                                             final int slotIndex,
                                             final int containerIndex) {
        networkManager.send(
            player,
            new ResourceFilterSlotUpdatePacket(slotIndex, containerIndex, resourceFilterContainer)
        );
    }

    @Override
    public void sendStorageInfoResponse(final ServerPlayer player,
                                        final UUID id,
                                        final StorageInfo storageInfo) {
        networkManager.send(player, new StorageInfoResponsePacket(id, storageInfo.stored(), storageInfo.capacity()));
    }
}
