package com.refinedmods.refinedstorage2.platform.forge.support.packet.s2c;

import com.refinedmods.refinedstorage2.platform.common.support.energy.EnergyContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record EnergyInfoPacket(long stored, long capacity) implements CustomPacketPayload {
    public static EnergyInfoPacket decode(final FriendlyByteBuf buf) {
        return new EnergyInfoPacket(buf.readLong(), buf.readLong());
    }

    public static void handle(final EnergyInfoPacket packet, final PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            final AbstractContainerMenu menu = player.containerMenu;
            if (menu instanceof EnergyContainerMenu energy) {
                energy.getEnergyInfo().setEnergy(packet.stored, packet.capacity);
            }
        }));
    }

    @Override
    public void write(final FriendlyByteBuf buf) {
        buf.writeLong(stored);
        buf.writeLong(capacity);
    }

    @Override
    public ResourceLocation id() {
        return PacketIds.ENERGY_INFO;
    }
}
