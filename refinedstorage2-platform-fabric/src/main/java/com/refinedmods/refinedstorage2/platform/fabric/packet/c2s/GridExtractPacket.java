package com.refinedmods.refinedstorage2.platform.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.GridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class GridExtractPacket implements ServerPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(final MinecraftServer server,
                        final ServerPlayer player,
                        final ServerGamePacketListenerImpl handler,
                        final FriendlyByteBuf buf,
                        final PacketSender responseSender) {
        final ResourceLocation id = buf.readResourceLocation();
        PlatformApi.INSTANCE.getStorageChannelTypeRegistry()
            .get(id)
            .ifPresent(type -> handle(type, buf, player, server));
    }

    private <T> void handle(final PlatformStorageChannelType<T> type,
                            final FriendlyByteBuf buf,
                            final Player player,
                            final MinecraftServer server) {
        final AbstractContainerMenu menu = player.containerMenu;
        if (menu instanceof GridExtractionStrategy strategy) {
            final GridExtractMode mode = getMode(buf.readByte());
            final boolean cursor = buf.readBoolean();
            final T resource = type.fromBuffer(buf);
            server.execute(() -> strategy.onExtract(type, resource, mode, cursor));
        }
    }

    private static GridExtractMode getMode(final byte mode) {
        if (mode == 0) {
            return GridExtractMode.ENTIRE_RESOURCE;
        }
        return GridExtractMode.HALF_RESOURCE;
    }

    public static void writeMode(final FriendlyByteBuf buf, final GridExtractMode mode) {
        if (mode == GridExtractMode.ENTIRE_RESOURCE) {
            buf.writeByte(0);
        } else if (mode == GridExtractMode.HALF_RESOURCE) {
            buf.writeByte(1);
        }
    }
}
