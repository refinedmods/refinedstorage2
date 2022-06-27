package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.platform.common.containermenu.ResourceFilterableContainerMenu;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class ResourceTypeChangePacket {
    private final ResourceLocation id;

    public ResourceTypeChangePacket(final ResourceLocation id) {
        this.id = id;
    }

    public static ResourceTypeChangePacket decode(final FriendlyByteBuf buf) {
        return new ResourceTypeChangePacket(buf.readResourceLocation());
    }

    public static void encode(final ResourceTypeChangePacket packet, final FriendlyByteBuf buf) {
        buf.writeResourceLocation(packet.id);
    }

    public static void handle(final ResourceTypeChangePacket packet, final Supplier<NetworkEvent.Context> ctx) {
        final ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            ctx.get().enqueueWork(() -> handle(packet, player));
        }
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final ResourceTypeChangePacket packet, final Player player) {
        if (player.containerMenu instanceof ResourceFilterableContainerMenu resourceFilterable) {
            resourceFilterable.setCurrentResourceType(packet.id);
        }
    }
}
