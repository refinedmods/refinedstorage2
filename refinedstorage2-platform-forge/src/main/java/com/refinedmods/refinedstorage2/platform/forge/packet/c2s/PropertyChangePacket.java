package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class PropertyChangePacket {
    private final int id;
    private final int value;

    public PropertyChangePacket(final int id, final int value) {
        this.id = id;
        this.value = value;
    }

    public static PropertyChangePacket decode(final FriendlyByteBuf buf) {
        return new PropertyChangePacket(buf.readInt(), buf.readInt());
    }

    public static void encode(final PropertyChangePacket packet, final FriendlyByteBuf buf) {
        buf.writeInt(packet.id);
        buf.writeInt(packet.value);
    }

    public static void handle(final PropertyChangePacket packet, final Supplier<NetworkEvent.Context> ctx) {
        final ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            ctx.get().enqueueWork(() -> handle(packet, player));
        }
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final PropertyChangePacket packet, final Player player) {
        player.containerMenu.setData(packet.id, packet.value);
    }
}
