package com.refinedmods.refinedstorage2.platform.fabric.packet.s2c;

import com.refinedmods.refinedstorage2.platform.common.support.energy.EnergyContainerMenu;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

public class EnergyInfoPacket implements ClientPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(final Minecraft client,
                        final ClientPacketListener handler,
                        final FriendlyByteBuf buf,
                        final PacketSender responseSender) {
        final long stored = buf.readLong();
        final long capacity = buf.readLong();

        client.execute(() -> {
            if (client.player.containerMenu instanceof EnergyContainerMenu energyContainer) {
                energyContainer.getEnergyInfo().setEnergy(stored, capacity);
            }
        });
    }
}
