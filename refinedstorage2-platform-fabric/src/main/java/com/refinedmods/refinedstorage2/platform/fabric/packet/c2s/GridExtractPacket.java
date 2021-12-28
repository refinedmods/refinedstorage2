package com.refinedmods.refinedstorage2.platform.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.fluid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.item.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketUtil;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class GridExtractPacket implements ServerPlayNetworking.PlayChannelHandler {
    public static void writeMode(FriendlyByteBuf buf, GridExtractMode mode) {
        switch (mode) {
            case ENTIRE_RESOURCE -> buf.writeByte(0);
            case HALF_RESOURCE -> buf.writeByte(1);
        }
    }

    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        GridExtractMode mode = getMode(buf.readByte());
        boolean cursor = buf.readBoolean();

        AbstractContainerMenu screenHandler = player.containerMenu;
        if (screenHandler instanceof ItemGridEventHandler itemGridEventHandler) {
            ItemResource itemResource = PacketUtil.readItemResource(buf);
            server.execute(() -> itemGridEventHandler.onExtract(itemResource, mode, cursor));
        } else if (screenHandler instanceof FluidGridEventHandler fluidGridEventHandler) {
            FluidResource fluidResource = PacketUtil.readFluidResource(buf);
            server.execute(() -> fluidGridEventHandler.onExtract(fluidResource, mode, cursor));
        }
    }

    private GridExtractMode getMode(byte mode) {
        if (mode == 0) {
            return GridExtractMode.ENTIRE_RESOURCE;
        }
        return GridExtractMode.HALF_RESOURCE;
    }
}
