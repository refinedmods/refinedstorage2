package com.refinedmods.refinedstorage2.platform.fabric.packet.s2c;

import com.refinedmods.refinedstorage2.platform.common.containermenu.ControllerContainerMenu;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

public class ControllerEnergyPacket implements ClientPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(final Minecraft client,
                        final ClientPacketListener handler,
                        final FriendlyByteBuf buf,
                        final PacketSender responseSender) {
        final long stored = buf.readLong();
        final long capacity = buf.readLong();

        client.execute(() -> {
            if (client.player.containerMenu instanceof ControllerContainerMenu controller) {
                controller.setEnergy(stored, capacity);
            }
        });
    }
}
