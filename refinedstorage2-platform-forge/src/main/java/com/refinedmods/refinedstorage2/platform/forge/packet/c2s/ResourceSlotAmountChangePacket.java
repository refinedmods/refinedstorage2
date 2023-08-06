package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractResourceContainerMenu;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class ResourceSlotAmountChangePacket {
    private final int slotIndex;
    private final long amount;

    public ResourceSlotAmountChangePacket(final int slotIndex, final long amount) {
        this.slotIndex = slotIndex;
        this.amount = amount;
    }

    public static ResourceSlotAmountChangePacket decode(final FriendlyByteBuf buf) {
        return new ResourceSlotAmountChangePacket(buf.readInt(), buf.readLong());
    }

    public static void encode(final ResourceSlotAmountChangePacket packet, final FriendlyByteBuf buf) {
        buf.writeInt(packet.slotIndex);
        buf.writeLong(packet.amount);
    }

    public static void handle(final ResourceSlotAmountChangePacket packet, final Supplier<NetworkEvent.Context> ctx) {
        final ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            ctx.get().enqueueWork(() -> handle(packet, player));
        }
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final ResourceSlotAmountChangePacket packet, final Player player) {
        if (player.containerMenu instanceof AbstractResourceContainerMenu containerMenu) {
            containerMenu.handleResourceSlotAmountChange(packet.slotIndex, packet.amount);
        }
    }
}
