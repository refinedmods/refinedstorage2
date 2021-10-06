package com.refinedmods.refinedstorage2.platform.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.item.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.fabric.util.PacketUtil;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class GridExtractPacket implements ServerPlayNetworking.PlayChannelHandler {
    public static void writeMode(PacketByteBuf buf, GridExtractMode mode) {
        switch (mode) {
            case ENTIRE_RESOURCE -> buf.writeByte(0);
            case HALF_RESOURCE -> buf.writeByte(1);
        }
    }

    @Override
    public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        ItemResource itemResource = PacketUtil.readItemResource(buf);
        GridExtractMode mode = getMode(buf.readByte());
        boolean cursor = buf.readBoolean();

        server.execute(() -> {
            ScreenHandler screenHandler = player.currentScreenHandler;
            if (screenHandler instanceof ItemGridEventHandler itemGridEventHandler) {
                itemGridEventHandler.onExtract(itemResource, mode, cursor);
            }
        });
    }

    private GridExtractMode getMode(byte mode) {
        if (mode == 0) {
            return GridExtractMode.ENTIRE_RESOURCE;
        }
        return GridExtractMode.HALF_RESOURCE;
    }
}
