package com.refinedmods.refinedstorage2.platform.fabric.packet.s2c;

import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class GridUpdatePacket implements ClientPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(final Minecraft client,
                        final ClientPacketListener handler,
                        final FriendlyByteBuf buf,
                        final PacketSender responseSender) {
        final ResourceLocation id = buf.readResourceLocation();
        PlatformApi.INSTANCE.getStorageChannelTypeRegistry().get(id).ifPresent(type -> handle(type, buf, client));
    }

    private <T> void handle(final PlatformStorageChannelType<T> type,
                            final FriendlyByteBuf buf,
                            final Minecraft client) {
        final T resource = type.fromBuffer(buf);
        final long amount = buf.readLong();
        final TrackedResource trackedResource = PacketUtil.readTrackedResource(buf);
        if (client.player.containerMenu instanceof AbstractGridContainerMenu containerMenu) {
            containerMenu.onResourceUpdate(resource, amount, trackedResource);
        }
    }
}
