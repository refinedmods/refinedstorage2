package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.platform.common.controller.ControllerContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.util.ClientProxy;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class ControllerEnergyInfoPacket {
    private final long stored;
    private final long capacity;

    public ControllerEnergyInfoPacket(final long stored, final long capacity) {
        this.stored = stored;
        this.capacity = capacity;
    }

    public static ControllerEnergyInfoPacket decode(final FriendlyByteBuf buf) {
        return new ControllerEnergyInfoPacket(buf.readLong(), buf.readLong());
    }

    public static void encode(final ControllerEnergyInfoPacket packet, final FriendlyByteBuf buf) {
        buf.writeLong(packet.stored);
        buf.writeLong(packet.capacity);
    }

    public static void handle(final ControllerEnergyInfoPacket packet, final Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientProxy.getPlayer().ifPresent(player -> handle(player, packet)));
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final Player player, final ControllerEnergyInfoPacket packet) {
        final AbstractContainerMenu menu = player.containerMenu;
        if (menu instanceof ControllerContainerMenu controller) {
            controller.setEnergyInfo(packet.stored, packet.capacity);
        }
    }
}
