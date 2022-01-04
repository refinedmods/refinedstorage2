package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.platform.common.containermenu.ControllerContainerMenu;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class ControllerEnergyPacket {
    private final long stored;
    private final long capacity;

    public ControllerEnergyPacket(long stored, long capacity) {
        this.stored = stored;
        this.capacity = capacity;
    }

    public static ControllerEnergyPacket decode(FriendlyByteBuf buf) {
        return new ControllerEnergyPacket(buf.readLong(), buf.readLong());
    }

    public static void encode(ControllerEnergyPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.stored);
        buf.writeLong(packet.capacity);
    }

    public static void handle(ControllerEnergyPacket packet, Supplier<NetworkEvent.Context> ctx) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ctx.get().enqueueWork(() -> handle(packet, player));
        }
        ctx.get().setPacketHandled(true);
    }

    private static void handle(ControllerEnergyPacket packet, Player player) {
        AbstractContainerMenu screenHandler = player.containerMenu;
        if (screenHandler instanceof ControllerContainerMenu controllerScreenHandler) {
            controllerScreenHandler.setEnergy(packet.stored, packet.capacity);
        }
    }
}
