package com.refinedmods.refinedstorage.platform.common.support.packet.c2s;

import com.refinedmods.refinedstorage.platform.common.support.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage.platform.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;

public record PropertyChangePacket(ResourceLocation propertyId, int value) implements CustomPacketPayload {
    public static final Type<PropertyChangePacket> PACKET_TYPE = new Type<>(createIdentifier("property_change"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PropertyChangePacket> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC, PropertyChangePacket::propertyId,
        ByteBufCodecs.INT, PropertyChangePacket::value,
        PropertyChangePacket::new
    );

    public static void handle(final PropertyChangePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractBaseContainerMenu menu) {
            menu.receivePropertyChangeFromClient(packet.propertyId, packet.value);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
