package com.refinedmods.refinedstorage2.platform.fabric.packet.s2c;

import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.FabricClientStorageRepository;

import java.util.UUID;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class StorageInfoResponsePacket implements ClientPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        UUID id = buf.readUuid();
        long stored = buf.readLong();
        long capacity = buf.readLong();

        client.execute(() -> ((FabricClientStorageRepository) Rs2PlatformApiFacade.INSTANCE.getStorageRepository(client.world)).setInfo(id, stored, capacity));
    }
}
