package com.refinedmods.refinedstorage2.platform.fabric.packet.s2c;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.ItemGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class GridItemUpdatePacket implements ClientPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        ItemResource resource = PacketUtil.readItemResource(buf);
        long amount = buf.readLong();
        StorageTracker.Entry trackerEntry = PacketUtil.readTrackerEntry(buf);

        client.execute(() -> {
            AbstractContainerMenu screenHandler = client.player.containerMenu;
            if (screenHandler instanceof ItemGridContainerMenu itemGridContainerMenu) {
                itemGridContainerMenu.onResourceUpdate(resource, amount, trackerEntry);
            }
        });
    }
}
