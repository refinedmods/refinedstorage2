package com.refinedmods.refinedstorage2.platform.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.apiimpl.grid.GridScrollModeUtil;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class GridScrollPacket implements ServerPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(final MinecraftServer server,
                        final ServerPlayer player,
                        final ServerGamePacketListenerImpl handler,
                        final FriendlyByteBuf buf,
                        final PacketSender responseSender) {
        final ItemResource itemResource = PacketUtil.readItemResource(buf);
        final GridScrollMode mode = GridScrollModeUtil.getMode(buf.readByte());
        final int slot = buf.readInt();

        server.execute(() -> {
            if (player.containerMenu instanceof ItemGridEventHandler gridEventHandler) {
                gridEventHandler.onScroll(itemResource, mode, slot);
            }
        });
    }
}
