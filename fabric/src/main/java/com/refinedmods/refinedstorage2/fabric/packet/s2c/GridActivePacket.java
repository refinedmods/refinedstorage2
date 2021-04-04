package com.refinedmods.refinedstorage2.fabric.packet.s2c;

import com.refinedmods.refinedstorage2.core.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

public class GridActivePacket implements ClientPlayNetworking.PlayChannelHandler {
    public static final Identifier ID = RefinedStorage2Mod.createIdentifier("grid_active");

    @Override
    public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        boolean active = buf.readBoolean();

        client.execute(() -> {
            ScreenHandler screenHandler = client.player.currentScreenHandler;
            if (screenHandler instanceof GridEventHandler) {
                ((GridEventHandler) screenHandler).onActiveChanged(active);
            }
        });
    }
}
