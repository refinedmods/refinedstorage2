package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.platform.common.containermenu.ControllerContainerMenu;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class ControllerEnergyPacket {
    private final long stored;
    private final long capacity;

    public ControllerEnergyPacket(final long stored, final long capacity) {
        this.stored = stored;
        this.capacity = capacity;
    }

    public static ControllerEnergyPacket decode(final FriendlyByteBuf buf) {
        return new ControllerEnergyPacket(buf.readLong(), buf.readLong());
    }

    public static void encode(final ControllerEnergyPacket packet, final FriendlyByteBuf buf) {
        buf.writeLong(packet.stored);
        buf.writeLong(packet.capacity);
    }

    public static void handle(final ControllerEnergyPacket packet, final Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientProxy.getPlayer().ifPresent(player -> handle(player, packet)));
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final Player player, final ControllerEnergyPacket packet) {
        final AbstractContainerMenu menu = player.containerMenu;
        if (menu instanceof ControllerContainerMenu controller) {
            controller.setEnergy(packet.stored, packet.capacity);
        }
    }
}
