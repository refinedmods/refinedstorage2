package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.platform.common.support.energy.EnergyContainerMenu;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class EnergyInfoPacket {
    private final long stored;
    private final long capacity;

    public EnergyInfoPacket(final long stored, final long capacity) {
        this.stored = stored;
        this.capacity = capacity;
    }

    public static EnergyInfoPacket decode(final FriendlyByteBuf buf) {
        return new EnergyInfoPacket(buf.readLong(), buf.readLong());
    }

    public static void encode(final EnergyInfoPacket packet, final FriendlyByteBuf buf) {
        buf.writeLong(packet.stored);
        buf.writeLong(packet.capacity);
    }

    public static void handle(final EnergyInfoPacket packet, final Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientProxy.getPlayer().ifPresent(player -> handle(player, packet)));
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final Player player, final EnergyInfoPacket packet) {
        final AbstractContainerMenu menu = player.containerMenu;
        if (menu instanceof EnergyContainerMenu energy) {
            energy.getEnergyInfo().setEnergy(packet.stored, packet.capacity);
        }
    }
}
