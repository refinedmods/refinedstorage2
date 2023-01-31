package com.refinedmods.refinedstorage2.platform.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractResourceFilterContainerMenu;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class ResourceFilterSlotChangePacket implements ServerPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(final MinecraftServer server,
                        final ServerPlayer player,
                        final ServerGamePacketListenerImpl handler,
                        final FriendlyByteBuf buf,
                        final PacketSender responseSender) {
        final int slotIndex = buf.readInt();
        final boolean tryAlternatives = buf.readBoolean();
        server.execute(() -> {
            if (player.containerMenu instanceof AbstractResourceFilterContainerMenu resourceFilterContainerMenu) {
                resourceFilterContainerMenu.handleResourceFilterSlotChange(slotIndex, tryAlternatives);
            }
        });
    }
}
