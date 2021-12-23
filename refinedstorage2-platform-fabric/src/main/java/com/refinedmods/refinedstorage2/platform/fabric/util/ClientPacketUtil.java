package com.refinedmods.refinedstorage2.platform.fabric.util;

import java.util.function.Consumer;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class ClientPacketUtil {
    private ClientPacketUtil() {
    }

    public static void sendToServer(ResourceLocation id, Consumer<FriendlyByteBuf> bufConsumer) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        bufConsumer.accept(buf);
        ClientPlayNetworking.send(id, buf);
    }
}
