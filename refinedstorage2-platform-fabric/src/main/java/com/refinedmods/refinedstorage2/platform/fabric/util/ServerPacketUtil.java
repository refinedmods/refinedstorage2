package com.refinedmods.refinedstorage2.platform.fabric.util;

import java.util.function.Consumer;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class ServerPacketUtil {
    private ServerPacketUtil() {
    }

    public static void sendToPlayer(ServerPlayer playerEntity, ResourceLocation id, Consumer<FriendlyByteBuf> bufConsumer) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        bufConsumer.accept(buf);
        ServerPlayNetworking.send(playerEntity, id, buf);
    }
}
