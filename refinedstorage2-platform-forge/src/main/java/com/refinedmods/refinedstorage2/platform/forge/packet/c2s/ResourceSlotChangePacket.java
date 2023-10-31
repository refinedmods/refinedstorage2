package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.platform.common.support.containermenu.AbstractResourceContainerMenu;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class ResourceSlotChangePacket {
    private final int slotIndex;
    private final boolean tryAlternatives;

    public ResourceSlotChangePacket(final int slotIndex, final boolean tryAlternatives) {
        this.slotIndex = slotIndex;
        this.tryAlternatives = tryAlternatives;
    }

    public static ResourceSlotChangePacket decode(final FriendlyByteBuf buf) {
        return new ResourceSlotChangePacket(buf.readInt(), buf.readBoolean());
    }

    public static void encode(final ResourceSlotChangePacket packet, final FriendlyByteBuf buf) {
        buf.writeInt(packet.slotIndex);
        buf.writeBoolean(packet.tryAlternatives);
    }

    public static void handle(final ResourceSlotChangePacket packet,
                              final Supplier<NetworkEvent.Context> ctx) {
        final ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            ctx.get().enqueueWork(() -> handle(packet, player));
        }
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final ResourceSlotChangePacket packet, final Player player) {
        if (player.containerMenu instanceof AbstractResourceContainerMenu containerMenu) {
            containerMenu.handleResourceSlotChange(packet.slotIndex, packet.tryAlternatives);
        }
    }
}
