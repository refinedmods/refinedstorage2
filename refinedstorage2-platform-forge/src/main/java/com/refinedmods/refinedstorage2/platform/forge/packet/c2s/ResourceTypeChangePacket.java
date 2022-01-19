package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.platform.common.containermenu.ResourceFilterableContainerMenu;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class ResourceTypeChangePacket {
    private final ResourceLocation id;

    public ResourceTypeChangePacket(ResourceLocation id) {
        this.id = id;
    }

    public static ResourceTypeChangePacket decode(FriendlyByteBuf buf) {
        return new ResourceTypeChangePacket(buf.readResourceLocation());
    }

    public static void encode(ResourceTypeChangePacket packet, FriendlyByteBuf buf) {
        buf.writeResourceLocation(packet.id);
    }

    public static void handle(ResourceTypeChangePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            ctx.get().enqueueWork(() -> handle(packet, player));
        }
        ctx.get().setPacketHandled(true);
    }

    private static void handle(ResourceTypeChangePacket packet, Player player) {
        AbstractContainerMenu screenHandler = player.containerMenu;
        if (screenHandler instanceof ResourceFilterableContainerMenu containerMenu) {
            containerMenu.setCurrentResourceType(packet.id);
        }
    }
}
