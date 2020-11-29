package com.refinedmods.refinedstorage2.fabric.util;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class PacketUtil {
    public static void sendToServer(Identifier id, Consumer<PacketByteBuf> bufConsumer) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        bufConsumer.accept(buf);
        ClientSidePacketRegistry.INSTANCE.sendToServer(id, buf);
    }

    public static void sendToPlayer(PlayerEntity playerEntity, Identifier id, Consumer<PacketByteBuf> bufConsumer) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        bufConsumer.accept(buf);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(playerEntity, id, buf);
    }
}
