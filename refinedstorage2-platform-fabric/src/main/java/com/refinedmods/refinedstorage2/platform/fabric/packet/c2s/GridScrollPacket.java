package com.refinedmods.refinedstorage2.platform.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridScrollingStrategy;
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

public class GridScrollPacket implements ServerPlayNetworking.PlayChannelHandler {
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
        if (menu instanceof GridScrollingStrategy strategy) {
            final GridScrollMode mode = getMode(buf.readByte());
            final int slotIndex = buf.readInt();
            final T resource = type.fromBuffer(buf);
            server.execute(() -> strategy.onScroll(type, resource, mode, slotIndex));
        }
    }

    public static GridScrollMode getMode(final byte mode) {
        if (mode == 0) {
            return GridScrollMode.GRID_TO_INVENTORY;
        } else if (mode == 1) {
            return GridScrollMode.GRID_TO_CURSOR;
        }
        return GridScrollMode.INVENTORY_TO_GRID;
    }

    public static void writeMode(final FriendlyByteBuf buf, final GridScrollMode mode) {
        switch (mode) {
            case GRID_TO_INVENTORY -> buf.writeByte(0);
            case GRID_TO_CURSOR -> buf.writeByte(1);
            case INVENTORY_TO_GRID -> buf.writeByte(2);
        }
    }
}
