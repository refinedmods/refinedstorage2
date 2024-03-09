package com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s;

import com.refinedmods.refinedstorage2.platform.common.support.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record PropertyChangePacket(ResourceLocation propertyId, int value) implements CustomPacketPayload {
    public static PropertyChangePacket decode(final FriendlyByteBuf buf) {
        return new PropertyChangePacket(buf.readResourceLocation(), buf.readInt());
    }

    public static void handle(final PropertyChangePacket packet, final PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            if (player.containerMenu instanceof AbstractBaseContainerMenu menu) {
                menu.receivePropertyChangeFromClient(packet.propertyId, packet.value);
            }
        }));
    }

    @Override
    public void write(final FriendlyByteBuf buf) {
        buf.writeResourceLocation(propertyId);
        buf.writeInt(value);
    }

    @Override
    public ResourceLocation id() {
        return PacketIds.PROPERTY_CHANGE;
    }
}
