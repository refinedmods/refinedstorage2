package com.refinedmods.refinedstorage2.platform.fabric.packet.s2c;

import com.refinedmods.refinedstorage2.platform.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.ClientStorageRepository;

import java.util.UUID;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

public class StorageInfoResponsePacket implements ClientPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        UUID id = buf.readUUID();
        long stored = buf.readLong();
        long capacity = buf.readLong();

        client.execute(() -> ((ClientStorageRepository) Rs2PlatformApiFacade.INSTANCE.getStorageRepository(client.level)).setInfo(id, stored, capacity));
    }
}
