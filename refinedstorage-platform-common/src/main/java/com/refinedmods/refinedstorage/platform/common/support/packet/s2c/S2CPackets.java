package com.refinedmods.refinedstorage.platform.common.support.packet.s2c;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.platform.api.storage.StorageInfo;
import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.networking.NetworkTransmitterData;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class S2CPackets {
    private S2CPackets() {
    }

    public static void sendEnergyInfo(final ServerPlayer player, final long stored, final long capacity) {
        Platform.INSTANCE.sendPacketToClient(player, new EnergyInfoPacket(stored, capacity));
    }

    public static void sendWirelessTransmitterRange(final ServerPlayer player, final int range) {
        Platform.INSTANCE.sendPacketToClient(player, new WirelessTransmitterRangePacket(range));
    }

    public static void sendGridActiveness(final ServerPlayer player, final boolean active) {
        Platform.INSTANCE.sendPacketToClient(player, new GridActivePacket(active));
    }

    public static void sendGridUpdate(final ServerPlayer player,
                                      final PlatformResourceKey resource,
                                      final long change,
                                      @Nullable final TrackedResource trackedResource) {
        Platform.INSTANCE.sendPacketToClient(player, new GridUpdatePacket(
            resource,
            change,
            Optional.ofNullable(trackedResource)
        ));
    }

    public static void sendGridClear(final ServerPlayer player) {
        Platform.INSTANCE.sendPacketToClient(player, GridClearPacket.INSTANCE);
    }

    public static void sendResourceSlotUpdate(final ServerPlayer player,
                                              @Nullable final ResourceAmount resourceAmount,
                                              final int slotIndex) {
        Platform.INSTANCE.sendPacketToClient(player, new ResourceSlotUpdatePacket(
            slotIndex,
            Optional.ofNullable(resourceAmount)
        ));
    }

    public static void sendStorageInfoResponse(final ServerPlayer player,
                                               final UUID id,
                                               final StorageInfo storageInfo) {
        Platform.INSTANCE.sendPacketToClient(
            player,
            new StorageInfoResponsePacket(id, storageInfo.stored(), storageInfo.capacity())
        );
    }

    public static void sendNetworkTransmitterStatus(final ServerPlayer player, final NetworkTransmitterData status) {
        Platform.INSTANCE.sendPacketToClient(
            player,
            new NetworkTransmitterStatusPacket(status.error(), status.message())
        );
    }

    public static void sendNoPermission(final ServerPlayer player, final Component message) {
        Platform.INSTANCE.sendPacketToClient(player, new NoPermissionPacket(message));
    }
}
