package com.refinedmods.refinedstorage2.platform.fabric.packet.s2c;

import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.FluidGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

public class GridFluidUpdatePacket implements ClientPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(final Minecraft client, final ClientPacketListener handler, final FriendlyByteBuf buf, final PacketSender responseSender) {
        final FluidResource fluidResource = PacketUtil.readFluidResource(buf);
        final long amount = buf.readLong();
        final TrackedResource trackedResource = PacketUtil.readTrackedResource(buf);

        client.execute(() -> {
            if (client.player.containerMenu instanceof FluidGridContainerMenu fluidGrid) {
                fluidGrid.onResourceUpdate(fluidResource, amount, trackedResource);
            }
        });
    }
}
