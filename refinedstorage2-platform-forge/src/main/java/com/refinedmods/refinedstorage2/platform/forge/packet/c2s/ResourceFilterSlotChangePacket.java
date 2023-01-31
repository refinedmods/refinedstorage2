package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractResourceFilterContainerMenu;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class ResourceFilterSlotChangePacket {
    private final int slotIndex;
    private final boolean tryAlternatives;

    public ResourceFilterSlotChangePacket(final int slotIndex, final boolean tryAlternatives) {
        this.slotIndex = slotIndex;
        this.tryAlternatives = tryAlternatives;
    }

    public static ResourceFilterSlotChangePacket decode(final FriendlyByteBuf buf) {
        return new ResourceFilterSlotChangePacket(buf.readInt(), buf.readBoolean());
    }

    public static void encode(final ResourceFilterSlotChangePacket packet, final FriendlyByteBuf buf) {
        buf.writeInt(packet.slotIndex);
        buf.writeBoolean(packet.tryAlternatives);
    }

    public static void handle(final ResourceFilterSlotChangePacket packet,
                              final Supplier<NetworkEvent.Context> ctx) {
        final ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            ctx.get().enqueueWork(() -> handle(packet, player));
        }
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final ResourceFilterSlotChangePacket packet, final Player player) {
        if (player.containerMenu instanceof AbstractResourceFilterContainerMenu containerMenu) {
            containerMenu.handleResourceFilterSlotChange(packet.slotIndex, packet.tryAlternatives);
        }
    }
}
