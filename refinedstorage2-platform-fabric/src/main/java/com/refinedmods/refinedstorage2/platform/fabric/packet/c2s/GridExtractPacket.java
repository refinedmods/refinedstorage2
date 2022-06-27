package com.refinedmods.refinedstorage2.platform.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.platform.api.grid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.grid.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class GridExtractPacket implements ServerPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(final MinecraftServer server, final ServerPlayer player, final ServerGamePacketListenerImpl handler, final FriendlyByteBuf buf, final PacketSender responseSender) {
        final GridExtractMode mode = getMode(buf.readByte());
        final boolean cursor = buf.readBoolean();

        final AbstractContainerMenu menu = player.containerMenu;
        if (menu instanceof ItemGridEventHandler itemGridEventHandler) {
            final ItemResource itemResource = PacketUtil.readItemResource(buf);
            server.execute(() -> itemGridEventHandler.onExtract(itemResource, mode, cursor));
        } else if (menu instanceof FluidGridEventHandler fluidGridEventHandler) {
            final FluidResource fluidResource = PacketUtil.readFluidResource(buf);
            server.execute(() -> fluidGridEventHandler.onExtract(fluidResource, mode, cursor));
        }
    }

    private static GridExtractMode getMode(final byte mode) {
        if (mode == 0) {
            return GridExtractMode.ENTIRE_RESOURCE;
        }
        return GridExtractMode.HALF_RESOURCE;
    }

    public static void writeMode(final FriendlyByteBuf buf, final GridExtractMode mode) {
        if (mode == GridExtractMode.ENTIRE_RESOURCE) {
            buf.writeByte(0);
        } else if (mode == GridExtractMode.HALF_RESOURCE) {
            buf.writeByte(1);
        }
    }
}
