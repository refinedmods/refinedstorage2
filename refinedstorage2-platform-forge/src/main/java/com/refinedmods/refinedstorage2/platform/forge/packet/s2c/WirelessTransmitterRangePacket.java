package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.platform.common.wirelesstransmitter.WirelessTransmitterContainerMenu;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class WirelessTransmitterRangePacket {
    private final int range;

    public WirelessTransmitterRangePacket(final int range) {
        this.range = range;
    }

    public static WirelessTransmitterRangePacket decode(final FriendlyByteBuf buf) {
        return new WirelessTransmitterRangePacket(buf.readInt());
    }

    public static void encode(final WirelessTransmitterRangePacket packet, final FriendlyByteBuf buf) {
        buf.writeInt(packet.range);
    }

    public static void handle(final WirelessTransmitterRangePacket packet, final Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientProxy.getPlayer().ifPresent(player -> handle(player, packet)));
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final Player player, final WirelessTransmitterRangePacket packet) {
        final AbstractContainerMenu menu = player.containerMenu;
        if (menu instanceof WirelessTransmitterContainerMenu containerMenu) {
            containerMenu.setRange(packet.range);
        }
    }
}
