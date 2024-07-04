package com.refinedmods.refinedstorage.platform.common.support.packet.s2c;

import com.refinedmods.refinedstorage.platform.common.support.energy.EnergyContainerMenu;
import com.refinedmods.refinedstorage.platform.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;

public record EnergyInfoPacket(long stored, long capacity) implements CustomPacketPayload {
    public static final Type<EnergyInfoPacket> PACKET_TYPE = new Type<>(createIdentifier("energy_info"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EnergyInfoPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_LONG, EnergyInfoPacket::stored,
        ByteBufCodecs.VAR_LONG, EnergyInfoPacket::capacity,
        EnergyInfoPacket::new
    );

    public static void handle(final EnergyInfoPacket packet, final PacketContext ctx) {
        final AbstractContainerMenu menu = ctx.getPlayer().containerMenu;
        if (menu instanceof EnergyContainerMenu energy) {
            energy.getEnergyInfo().setEnergy(packet.stored, packet.capacity);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
