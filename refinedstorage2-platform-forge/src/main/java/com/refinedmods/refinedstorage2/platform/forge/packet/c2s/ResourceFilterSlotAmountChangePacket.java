package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractResourceFilterContainerMenu;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class ResourceFilterSlotAmountChangePacket {
    private final int slotIndex;
    private final long amount;

    public ResourceFilterSlotAmountChangePacket(final int slotIndex, final long amount) {
        this.slotIndex = slotIndex;
        this.amount = amount;
    }

    public static ResourceFilterSlotAmountChangePacket decode(final FriendlyByteBuf buf) {
        return new ResourceFilterSlotAmountChangePacket(buf.readInt(), buf.readLong());
    }

    public static void encode(final ResourceFilterSlotAmountChangePacket packet, final FriendlyByteBuf buf) {
        buf.writeInt(packet.slotIndex);
        buf.writeLong(packet.amount);
    }

    public static void handle(final ResourceFilterSlotAmountChangePacket packet,
                              final Supplier<NetworkEvent.Context> ctx) {
        final ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            ctx.get().enqueueWork(() -> handle(packet, player));
        }
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final ResourceFilterSlotAmountChangePacket packet, final Player player) {
        if (player.containerMenu instanceof AbstractResourceFilterContainerMenu resourceFilterable) {
            resourceFilterable.handleResourceFilterSlotAmountChange(packet.slotIndex, packet.amount);
        }
    }
}
