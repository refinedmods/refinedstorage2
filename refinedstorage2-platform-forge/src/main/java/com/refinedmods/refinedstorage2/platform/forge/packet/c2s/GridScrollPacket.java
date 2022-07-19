package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.GridScrollModeUtil;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class GridScrollPacket {
    private final ItemResource itemResource;
    private final GridScrollMode mode;
    private final int slotIndex;

    public GridScrollPacket(final ItemResource itemResource, final GridScrollMode mode, final int slotIndex) {
        this.itemResource = itemResource;
        this.mode = mode;
        this.slotIndex = slotIndex;
    }

    public static GridScrollPacket decode(final FriendlyByteBuf buf) {
        return new GridScrollPacket(
            PacketUtil.readItemResource(buf),
            GridScrollModeUtil.getMode(buf.readByte()),
            buf.readInt()
        );
    }

    public static void encode(final GridScrollPacket packet, final FriendlyByteBuf buf) {
        PacketUtil.writeItemResource(buf, packet.itemResource);
        GridScrollModeUtil.writeMode(buf, packet.mode);
        buf.writeInt(packet.slotIndex);
    }

    public static void handle(final GridScrollPacket packet, final Supplier<NetworkEvent.Context> ctx) {
        final ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            ctx.get().enqueueWork(() -> handle(packet, player));
        }
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final GridScrollPacket packet, final Player player) {
        if (player.containerMenu instanceof ItemGridEventHandler gridEventHandler) {
            gridEventHandler.onScroll(packet.itemResource, packet.mode, packet.slotIndex);
        }
    }
}
