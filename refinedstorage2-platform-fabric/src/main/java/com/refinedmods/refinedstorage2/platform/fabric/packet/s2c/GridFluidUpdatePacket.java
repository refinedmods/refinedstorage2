package com.refinedmods.refinedstorage2.platform.fabric.packet.s2c;

import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.grid.FluidGridScreenHandler;
import com.refinedmods.refinedstorage2.platform.fabric.util.PacketUtil;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;

public class GridFluidUpdatePacket implements ClientPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        Rs2FluidStack template = PacketUtil.readFluidStack(buf, false);
        long amount = buf.readLong();
        StorageTracker.Entry trackerEntry = PacketUtil.readTrackerEntry(buf);

        client.execute(() -> {
            ScreenHandler screenHandler = client.player.currentScreenHandler;
            if (screenHandler instanceof FluidGridScreenHandler fluidGridScreenHandler) {
                fluidGridScreenHandler.onStackUpdate(template, amount, trackerEntry);
            }
        });
    }
}
