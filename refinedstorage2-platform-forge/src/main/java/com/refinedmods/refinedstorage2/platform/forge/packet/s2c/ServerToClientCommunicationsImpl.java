package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceInstance;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
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
    public <T> void sendGridUpdate(final ServerPlayer player,
                                   final PlatformStorageChannelType<T> storageChannelType,
                                   final T resource,
                                   final long change,
                                   @Nullable final TrackedResource trackedResource) {
        PlatformApi.INSTANCE
            .getStorageChannelTypeRegistry()
            .getId(storageChannelType)
            .ifPresent(id -> networkManager.send(player, new GridUpdatePacket<>(
                storageChannelType,
                id,
                resource,
                change,
                trackedResource
            )));
    }

    @Override
    public void sendGridClear(final ServerPlayer player) {
        networkManager.send(player, new GridClearPacket());
    }

    @Override
    public <T> void sendResourceSlotUpdate(final ServerPlayer player,
                                           @Nullable final ResourceInstance<T> resourceInstance,
                                           final int slotIndex) {
        if (resourceInstance != null) {
            PlatformApi.INSTANCE.getStorageChannelTypeRegistry()
                .getId(resourceInstance.getStorageChannelType())
                .ifPresent(id -> networkManager.send(player, new ResourceSlotUpdatePacket<>(
                    slotIndex,
                    resourceInstance,
                    id
                )));
        } else {
            networkManager.send(player, new ResourceSlotUpdatePacket<>(
                slotIndex,
                null,
                null
            ));
        }
    }

    @Override
    public void sendStorageInfoResponse(final ServerPlayer player,
                                        final UUID id,
                                        final StorageInfo storageInfo) {
        networkManager.send(player, new StorageInfoResponsePacket(id, storageInfo.stored(), storageInfo.capacity()));
    }
}
