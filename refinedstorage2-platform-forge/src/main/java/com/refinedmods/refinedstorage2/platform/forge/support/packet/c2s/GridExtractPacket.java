package com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s;

import com.refinedmods.refinedstorage2.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record GridExtractPacket<T>(
    PlatformStorageChannelType<T> storageChannelType,
    ResourceLocation storageChannelTypeId,
    T resource,
    GridExtractMode mode,
    boolean cursor
) implements CustomPacketPayload {
    @SuppressWarnings("unchecked")
    public static GridExtractPacket<?> decode(final FriendlyByteBuf buf) {
        final ResourceLocation storageChannelTypeId = buf.readResourceLocation();
        final PlatformStorageChannelType<?> storageChannelType = PlatformApi.INSTANCE
            .getStorageChannelTypeRegistry()
            .get(storageChannelTypeId)
            .orElseThrow();
        final GridExtractMode mode = getMode(buf.readByte());
        final boolean cursor = buf.readBoolean();
        final Object resource = storageChannelType.fromBuffer(buf);
        return new GridExtractPacket<>(
            (PlatformStorageChannelType<? super Object>) storageChannelType,
            storageChannelTypeId,
            resource,
            mode,
            cursor
        );
    }

    public static <T> void handle(final GridExtractPacket<T> packet, final PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            if (player.containerMenu instanceof GridExtractionStrategy strategy) {
                strategy.onExtract(
                    packet.storageChannelType,
                    packet.resource,
                    packet.mode,
                    packet.cursor
                );
            }
        }));
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
        } else {
            buf.writeByte(1);
        }
    }

    @Override
    public void write(final FriendlyByteBuf buf) {
        buf.writeResourceLocation(storageChannelTypeId);
        writeMode(buf, mode);
        buf.writeBoolean(cursor);
        storageChannelType.toBuffer(resource, buf);
    }

    @Override
    public ResourceLocation id() {
        return PacketIds.GRID_EXTRACT;
    }
}
