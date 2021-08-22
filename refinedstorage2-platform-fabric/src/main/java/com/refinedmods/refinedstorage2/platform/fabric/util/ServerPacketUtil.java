package com.refinedmods.refinedstorage2.platform.fabric.util;

import java.util.function.Consumer;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ServerPacketUtil {
    private ServerPacketUtil() {
    }

    public static void sendToPlayer(ServerPlayerEntity playerEntity, Identifier id, Consumer<PacketByteBuf> bufConsumer) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        bufConsumer.accept(buf);
        ServerPlayNetworking.send(playerEntity, id, buf);
    }
}
