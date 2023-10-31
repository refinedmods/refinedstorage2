package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class GridScrollPacket<T> {
    private final PlatformStorageChannelType<T> storageChannelType;
    private final ResourceLocation storageChannelTypeId;
    private final T resource;
    private final GridScrollMode mode;
    private final int slotIndex;

    public GridScrollPacket(
        final PlatformStorageChannelType<T> storageChannelType,
        final ResourceLocation storageChannelTypeId,
        final T resource,
        final GridScrollMode mode,
        final int slotIndex
    ) {
        this.storageChannelType = storageChannelType;
        this.storageChannelTypeId = storageChannelTypeId;
        this.resource = resource;
        this.mode = mode;
        this.slotIndex = slotIndex;
    }

    @SuppressWarnings("unchecked")
    public static GridScrollPacket<?> decode(final FriendlyByteBuf buf) {
        final ResourceLocation storageChannelTypeId = buf.readResourceLocation();
        final PlatformStorageChannelType<?> storageChannelType = PlatformApi.INSTANCE
            .getStorageChannelTypeRegistry()
            .get(storageChannelTypeId)
            .orElseThrow();
        final GridScrollMode mode = getMode(buf.readByte());
        final int slotIndex = buf.readInt();
        final Object resource = storageChannelType.fromBuffer(buf);
        return new GridScrollPacket<>(
            (PlatformStorageChannelType<? super Object>) storageChannelType,
            storageChannelTypeId,
            resource,
            mode,
            slotIndex
        );
    }

    public static <T> void encode(final GridScrollPacket<T> packet, final FriendlyByteBuf buf) {
        buf.writeResourceLocation(packet.storageChannelTypeId);
        writeMode(buf, packet.mode);
        buf.writeInt(packet.slotIndex);
        packet.storageChannelType.toBuffer(packet.resource, buf);
    }

    public static <T> void handle(final GridScrollPacket<T> packet, final Supplier<NetworkEvent.Context> ctx) {
        final ServerPlayer player = ctx.get().getSender();
        if (player != null && player.containerMenu instanceof GridScrollingStrategy strategy) {
            strategy.onScroll(
                packet.storageChannelType,
                packet.resource,
                packet.mode,
                packet.slotIndex
            );
        }
        ctx.get().setPacketHandled(true);
    }

    private static GridScrollMode getMode(final byte mode) {
        if (mode == 0) {
            return GridScrollMode.GRID_TO_INVENTORY;
        } else if (mode == 1) {
            return GridScrollMode.GRID_TO_CURSOR;
        }
        return GridScrollMode.INVENTORY_TO_GRID;
    }

    private static void writeMode(final FriendlyByteBuf buf, final GridScrollMode mode) {
        switch (mode) {
            case GRID_TO_INVENTORY -> buf.writeByte(0);
            case GRID_TO_CURSOR -> buf.writeByte(1);
            case INVENTORY_TO_GRID -> buf.writeByte(2);
        }
    }
}
