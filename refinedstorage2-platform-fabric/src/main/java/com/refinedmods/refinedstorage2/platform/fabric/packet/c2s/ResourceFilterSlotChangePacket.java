package com.refinedmods.refinedstorage2.platform.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.AbstractResourceContainerMenu;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class ResourceFilterSlotChangePacket implements ServerPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(final MinecraftServer server,
                        final ServerPlayer player,
                        final ServerGamePacketListenerImpl handler,
                        final FriendlyByteBuf buf,
                        final PacketSender responseSender) {
        final int slotIndex = buf.readInt();
        final ResourceLocation resourceTypeId = buf.readResourceLocation();
        PlatformApi.INSTANCE.getResourceTypeRegistry()
            .get(resourceTypeId)
            .ifPresent(resourceType -> handle(resourceType, buf, server, slotIndex, player));
    }

    private void handle(final ResourceType resourceType,
                        final FriendlyByteBuf buf,
                        final MinecraftServer server,
                        final int slotIndex,
                        final ServerPlayer serverPlayer) {
        final PlatformResourceKey resource = resourceType.fromBuffer(buf);
        server.execute(() -> {
            if (serverPlayer.containerMenu instanceof AbstractResourceContainerMenu containerMenu) {
                containerMenu.handleResourceFilterSlotUpdate(slotIndex, resource);
            }
        });
    }
}
