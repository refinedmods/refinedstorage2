package com.refinedmods.refinedstorage2.platform.fabric.packet.s2c;

import com.refinedmods.refinedstorage2.platform.common.containermenu.ControllerContainerMenu;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class ControllerEnergyPacket implements ClientPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        long stored = buf.readLong();
        long capacity = buf.readLong();

        client.execute(() -> {
            AbstractContainerMenu screenHandler = client.player.containerMenu;
            if (screenHandler instanceof ControllerContainerMenu controllerScreenHandler) {
                controllerScreenHandler.setEnergy(stored, capacity);
            }
        });
    }
}
