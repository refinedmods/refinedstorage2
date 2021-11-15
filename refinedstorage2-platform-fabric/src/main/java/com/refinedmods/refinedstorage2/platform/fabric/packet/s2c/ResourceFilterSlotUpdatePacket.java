package com.refinedmods.refinedstorage2.platform.fabric.packet.s2c;

import com.refinedmods.refinedstorage2.platform.fabric.containermenu.ResourceFilterableContainerMenu;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

public class ResourceFilterSlotUpdatePacket implements ClientPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        int slotIndex = buf.readInt();
        // TODO: Can we move this off-thread?
        if (client.player.containerMenu instanceof ResourceFilterableContainerMenu containerMenu) {
            containerMenu.readResourceFilterSlotUpdate(slotIndex, buf);
        }
    }
}
