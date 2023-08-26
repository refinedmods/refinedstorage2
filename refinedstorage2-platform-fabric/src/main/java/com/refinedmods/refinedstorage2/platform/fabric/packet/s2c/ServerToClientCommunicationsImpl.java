package com.refinedmods.refinedstorage2.platform.fabric.packet.s2c;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.packet.ServerToClientCommunications;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;

import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ServerToClientCommunicationsImpl implements ServerToClientCommunications {
    @Override
    public void sendControllerEnergyInfo(final ServerPlayer player, final long stored, final long capacity) {
        sendToPlayer(player, PacketIds.CONTROLLER_ENERGY_INFO, buf -> {
            buf.writeLong(stored);
            buf.writeLong(capacity);
        });
    }

    @Override
    public void sendWirelessTransmitterRange(final ServerPlayer player, int range) {
        sendToPlayer(player, PacketIds.WIRELESS_TRANSMITTER_RANGE, buf -> buf.writeInt(range));
    }

    @Override
    public void sendGridActiveness(final ServerPlayer player, final boolean active) {
        sendToPlayer(player, PacketIds.GRID_ACTIVE, buf -> buf.writeBoolean(active));
    }

    @Override
    public <T> void sendGridUpdate(final ServerPlayer player,
                                   final PlatformStorageChannelType<T> storageChannelType,
                                   final T resource,
                                   final long change,
                                   @Nullable final TrackedResource trackedResource) {
        PlatformApi.INSTANCE.getStorageChannelTypeRegistry().getId(storageChannelType).ifPresent(id -> sendToPlayer(
            player,
            PacketIds.GRID_UPDATE,
            buf -> {
                buf.writeResourceLocation(id);
                storageChannelType.toBuffer(resource, buf);
                buf.writeLong(change);
                PacketUtil.writeTrackedResource(buf, trackedResource);
            }
        ));
    }

    @Override
    public void sendGridClear(final ServerPlayer player) {
        sendToPlayer(player, PacketIds.GRID_CLEAR, buf -> {
        });
    }

    @Override
    public <T> void sendResourceSlotUpdate(final ServerPlayer player,
                                           @Nullable final ResourceAmountTemplate<T> resourceAmount,
                                           final int slotIndex) {
        sendToPlayer(player, PacketIds.RESOURCE_SLOT_UPDATE, buf -> {
            buf.writeInt(slotIndex);
            if (resourceAmount != null) {
                sendResourceSlotUpdate(
                    resourceAmount.getStorageChannelType(),
                    resourceAmount.getResource(),
                    resourceAmount.getAmount(),
                    buf
                );
            } else {
                buf.writeBoolean(false);
            }
        });
    }

    private <T> void sendResourceSlotUpdate(final PlatformStorageChannelType<T> storageChannelType,
                                            final T resource,
                                            final long amount,
                                            final FriendlyByteBuf buf) {
        PlatformApi.INSTANCE.getStorageChannelTypeRegistry().getId(storageChannelType).ifPresentOrElse(id -> {
            buf.writeBoolean(true);
            buf.writeResourceLocation(id);
            storageChannelType.toBuffer(resource, buf);
            buf.writeLong(amount);
        }, () -> buf.writeBoolean(false));
    }

    @Override
    public void sendStorageInfoResponse(final ServerPlayer player, final UUID id, final StorageInfo storageInfo) {
        sendToPlayer(player, PacketIds.STORAGE_INFO_RESPONSE, bufToSend -> {
            bufToSend.writeUUID(id);
            bufToSend.writeLong(storageInfo.stored());
            bufToSend.writeLong(storageInfo.capacity());
        });
    }

    private static void sendToPlayer(final ServerPlayer playerEntity,
                                     final ResourceLocation id,
                                     final Consumer<FriendlyByteBuf> bufConsumer) {
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        bufConsumer.accept(buf);
        ServerPlayNetworking.send(playerEntity, id, buf);
    }
}
