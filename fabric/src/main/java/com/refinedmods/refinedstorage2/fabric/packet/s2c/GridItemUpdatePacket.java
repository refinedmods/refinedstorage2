package com.refinedmods.refinedstorage2.fabric.packet.s2c;

import com.refinedmods.refinedstorage2.core.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.core.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.fabric.util.PacketUtil;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;

public class GridItemUpdatePacket implements ClientPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        Rs2ItemStack template = PacketUtil.readItemStack(buf, false);
        long amount = buf.readLong();
        StorageTracker.Entry trackerEntry = PacketUtil.readTrackerEntry(buf);

        client.execute(() -> {
            ScreenHandler screenHandler = client.player.currentScreenHandler;
            if (screenHandler instanceof GridEventHandler gridEventHandler) {
                gridEventHandler.onItemUpdate(template, amount, trackerEntry);
            }
        });
    }
}
