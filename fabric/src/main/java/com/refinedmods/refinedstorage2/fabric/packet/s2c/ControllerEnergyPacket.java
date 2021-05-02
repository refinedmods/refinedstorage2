package com.refinedmods.refinedstorage2.fabric.packet.s2c;

import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.screenhandler.ControllerScreenHandler;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

public class ControllerEnergyPacket implements ClientPlayNetworking.PlayChannelHandler {
    public static final Identifier ID = Rs2Mod.createIdentifier("controller_energy");

    @Override
    public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        long stored = buf.readLong();
        long capacity = buf.readLong();

        client.execute(() -> {
            ScreenHandler screenHandler = client.player.currentScreenHandler;
            if (screenHandler instanceof ControllerScreenHandler) {
                ((ControllerScreenHandler) screenHandler).setEnergy(stored, capacity);
            }
        });
    }
}
