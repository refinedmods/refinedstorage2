package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.platform.common.networking.NetworkTransmitterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.networking.NetworkTransmitterStatus;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class NetworkTransmitterStatusPacket {
    private final boolean error;
    private final Component message;

    public NetworkTransmitterStatusPacket(final boolean error, final Component message) {
        this.error = error;
        this.message = message;
    }

    public static NetworkTransmitterStatusPacket decode(final FriendlyByteBuf buf) {
        return new NetworkTransmitterStatusPacket(buf.readBoolean(), buf.readComponent());
    }

    public static void encode(final NetworkTransmitterStatusPacket packet, final FriendlyByteBuf buf) {
        buf.writeBoolean(packet.error);
        buf.writeComponent(packet.message);
    }

    public static void handle(final NetworkTransmitterStatusPacket packet, final Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientProxy.getPlayer().ifPresent(player -> handle(player, packet)));
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final Player player, final NetworkTransmitterStatusPacket packet) {
        final AbstractContainerMenu menu = player.containerMenu;
        if (menu instanceof NetworkTransmitterContainerMenu containerMenu) {
            containerMenu.setStatus(new NetworkTransmitterStatus(packet.error, packet.message));
        }
    }
}
