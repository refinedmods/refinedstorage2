package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.platform.api.grid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.grid.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class GridExtractPacket {
    private final GridExtractMode mode;
    private final boolean cursor;
    private final ItemResource itemResource;
    private final FluidResource fluidResource;
    private final FriendlyByteBuf buf;

    public GridExtractPacket(GridExtractMode mode, boolean cursor, FriendlyByteBuf buf) {
        this.mode = mode;
        this.cursor = cursor;
        this.itemResource = null;
        this.fluidResource = null;
        this.buf = buf;
    }

    public GridExtractPacket(GridExtractMode mode, boolean cursor, ItemResource itemResource) {
        this.mode = mode;
        this.cursor = cursor;
        this.itemResource = itemResource;
        this.fluidResource = null;
        this.buf = null;
    }

    public GridExtractPacket(GridExtractMode mode, boolean cursor, FluidResource fluidResource) {
        this.mode = mode;
        this.cursor = cursor;
        this.itemResource = null;
        this.fluidResource = fluidResource;
        this.buf = null;
    }

    public static GridExtractPacket decode(FriendlyByteBuf buf) {
        return new GridExtractPacket(getMode(buf.readByte()), buf.readBoolean(), buf);
    }

    public static void encode(GridExtractPacket packet, FriendlyByteBuf buf) {
        writeMode(buf, packet.mode);
        buf.writeBoolean(packet.cursor);
        if (packet.itemResource != null) {
            PacketUtil.writeItemResource(buf, packet.itemResource);
        } else if (packet.fluidResource != null) {
            PacketUtil.writeFluidResource(buf, packet.fluidResource);
        }
    }

    public static void handle(GridExtractPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            AbstractContainerMenu screenHandler = player.containerMenu;
            if (screenHandler instanceof ItemGridEventHandler itemGridEventHandler) {
                ItemResource itemResource = PacketUtil.readItemResource(packet.buf);
                ctx.get().enqueueWork(() -> itemGridEventHandler.onExtract(itemResource, packet.mode, packet.cursor));
            } else if (screenHandler instanceof FluidGridEventHandler fluidGridEventHandler) {
                FluidResource fluidResource = PacketUtil.readFluidResource(packet.buf);
                ctx.get().enqueueWork(() -> fluidGridEventHandler.onExtract(fluidResource, packet.mode, packet.cursor));
            }
        }
        ctx.get().setPacketHandled(true);
    }

    private static GridExtractMode getMode(byte mode) {
        if (mode == 0) {
            return GridExtractMode.ENTIRE_RESOURCE;
        }
        return GridExtractMode.HALF_RESOURCE;
    }

    public static void writeMode(FriendlyByteBuf buf, GridExtractMode mode) {
        switch (mode) {
            case ENTIRE_RESOURCE -> buf.writeByte(0);
            case HALF_RESOURCE -> buf.writeByte(1);
        }
    }
}
