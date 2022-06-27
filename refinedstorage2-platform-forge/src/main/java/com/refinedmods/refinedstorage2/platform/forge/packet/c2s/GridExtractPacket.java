package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.platform.api.grid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.grid.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class GridExtractPacket {
    private final GridExtractMode mode;
    private final boolean cursor;
    @Nullable
    private final ItemResource itemResource;
    @Nullable
    private final FluidResource fluidResource;
    @Nullable
    private final FriendlyByteBuf buf;

    public GridExtractPacket(final GridExtractMode mode, final boolean cursor, final FriendlyByteBuf buf) {
        this.mode = mode;
        this.cursor = cursor;
        this.itemResource = null;
        this.fluidResource = null;
        this.buf = buf;
    }

    public GridExtractPacket(final GridExtractMode mode, final boolean cursor, final ItemResource itemResource) {
        this.mode = mode;
        this.cursor = cursor;
        this.itemResource = itemResource;
        this.fluidResource = null;
        this.buf = null;
    }

    public GridExtractPacket(final GridExtractMode mode, final boolean cursor, final FluidResource fluidResource) {
        this.mode = mode;
        this.cursor = cursor;
        this.itemResource = null;
        this.fluidResource = fluidResource;
        this.buf = null;
    }

    public static GridExtractPacket decode(final FriendlyByteBuf buf) {
        return new GridExtractPacket(getMode(buf.readByte()), buf.readBoolean(), buf);
    }

    public static void encode(final GridExtractPacket packet, final FriendlyByteBuf buf) {
        writeMode(buf, packet.mode);
        buf.writeBoolean(packet.cursor);
        if (packet.itemResource != null) {
            PacketUtil.writeItemResource(buf, packet.itemResource);
        } else if (packet.fluidResource != null) {
            PacketUtil.writeFluidResource(buf, packet.fluidResource);
        }
    }

    public static void handle(final GridExtractPacket packet, final Supplier<NetworkEvent.Context> ctx) {
        final ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            final AbstractContainerMenu menu = player.containerMenu;
            if (menu instanceof ItemGridEventHandler itemGridEventHandler) {
                ItemResource itemResource = PacketUtil.readItemResource(Objects.requireNonNull(packet.buf));
                ctx.get().enqueueWork(() -> itemGridEventHandler.onExtract(itemResource, packet.mode, packet.cursor));
            } else if (menu instanceof FluidGridEventHandler fluidGridEventHandler) {
                FluidResource fluidResource = PacketUtil.readFluidResource(Objects.requireNonNull(packet.buf));
                ctx.get().enqueueWork(() -> fluidGridEventHandler.onExtract(fluidResource, packet.mode, packet.cursor));
            }
        }
        ctx.get().setPacketHandled(true);
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
        } else {
            buf.writeByte(1);
        }
    }
}
