package com.refinedmods.refinedstorage2.platform.fabric.packet.s2c;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.ClientStorageRepository;

import java.util.UUID;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

public class StorageInfoResponsePacket implements ClientPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(final Minecraft client,
                        final ClientPacketListener handler,
                        final FriendlyByteBuf buf,
                        final PacketSender responseSender) {
        final UUID id = buf.readUUID();
        final long stored = buf.readLong();
        final long capacity = buf.readLong();

        if (client.level == null) {
            return;
        }

        client.execute(() -> ((ClientStorageRepository) PlatformApi.INSTANCE.getStorageRepository(client.level))
                .setInfo(id, stored, capacity));
    }
}
