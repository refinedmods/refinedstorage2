package com.refinedmods.refinedstorage2.platform.forge.support.packet.s2c;

import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.common.networking.NetworkTransmitterStatus;
import com.refinedmods.refinedstorage2.platform.common.support.ServerToClientCommunications;

import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class ServerToClientCommunicationsImpl implements ServerToClientCommunications {
    private void sendToPlayer(final ServerPlayer player, final CustomPacketPayload packet) {
        PacketDistributor.PLAYER.with(player).send(packet);
    }

    @Override
    public void sendEnergyInfo(final ServerPlayer player, final long stored, final long capacity) {
        sendToPlayer(player, new EnergyInfoPacket(stored, capacity));
    }

    @Override
    public void sendWirelessTransmitterRange(final ServerPlayer player, final int range) {
        sendToPlayer(player, new WirelessTransmitterRangePacket(range));
    }

    @Override
    public void sendGridActiveness(final ServerPlayer player, final boolean active) {
        sendToPlayer(player, new GridActivePacket(active));
    }

    @Override
    public void sendGridUpdate(final ServerPlayer player,
                               final PlatformStorageChannelType storageChannelType,
                               final ResourceKey resource,
                               final long change,
                               @Nullable final TrackedResource trackedResource) {
        PlatformApi.INSTANCE
            .getStorageChannelTypeRegistry()
            .getId(storageChannelType)
            .ifPresent(id -> sendToPlayer(player, new GridUpdatePacket(
                storageChannelType,
                id,
                resource,
                change,
                trackedResource
            )));
    }

    @Override
    public void sendGridClear(final ServerPlayer player) {
        sendToPlayer(player, new GridClearPacket());
    }

    @Override
    public void sendResourceSlotUpdate(final ServerPlayer player,
                                       @Nullable final ResourceAmountTemplate resourceAmount,
                                       final int slotIndex) {
        if (resourceAmount != null) {
            PlatformApi.INSTANCE.getStorageChannelTypeRegistry()
                .getId(resourceAmount.getStorageChannelType())
                .ifPresent(id -> sendToPlayer(player, new ResourceSlotUpdatePacket(
                    slotIndex,
                    resourceAmount,
                    id
                )));
        } else {
            sendToPlayer(player, new ResourceSlotUpdatePacket(
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
        sendToPlayer(player, new StorageInfoResponsePacket(id, storageInfo.stored(), storageInfo.capacity()));
    }

    @Override
    public void sendNetworkTransmitterStatus(final ServerPlayer player, final NetworkTransmitterStatus status) {
        sendToPlayer(player, new NetworkTransmitterStatusPacket(status.error(), status.message()));
    }
}
