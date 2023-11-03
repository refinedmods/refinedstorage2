package com.refinedmods.refinedstorage2.platform.fabric.packet.s2c;

import com.refinedmods.refinedstorage2.platform.common.networking.NetworkTransmitterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.networking.NetworkTransmitterStatus;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class NetworkTransmitterStatusPacket implements ClientPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(final Minecraft client,
                        final ClientPacketListener handler,
                        final FriendlyByteBuf buf,
                        final PacketSender responseSender) {
        final boolean error = buf.readBoolean();
        final Component message = buf.readComponent();

        client.execute(() -> {
            if (client.player.containerMenu instanceof NetworkTransmitterContainerMenu containerMenu) {
                containerMenu.setStatus(new NetworkTransmitterStatus(error, message));
            }
        });
    }
}
