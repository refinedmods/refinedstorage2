package com.refinedmods.refinedstorage2.platform.forge.support.packet.s2c;

import com.refinedmods.refinedstorage2.platform.common.networking.NetworkTransmitterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.networking.NetworkTransmitterStatus;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record NetworkTransmitterStatusPacket(boolean error, Component message) implements CustomPacketPayload {
    public static NetworkTransmitterStatusPacket decode(final FriendlyByteBuf buf) {
        return new NetworkTransmitterStatusPacket(buf.readBoolean(), buf.readComponent());
    }

    public static void handle(final NetworkTransmitterStatusPacket packet, final PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            final AbstractContainerMenu menu = player.containerMenu;
            if (menu instanceof NetworkTransmitterContainerMenu containerMenu) {
                containerMenu.setStatus(new NetworkTransmitterStatus(packet.error, packet.message));
            }
        }));
    }

    @Override
    public void write(final FriendlyByteBuf buf) {
        buf.writeBoolean(error);
        buf.writeComponent(message);
    }

    @Override
    public ResourceLocation id() {
        return PacketIds.NETWORK_TRANSMITTER_STATUS;
    }
}
