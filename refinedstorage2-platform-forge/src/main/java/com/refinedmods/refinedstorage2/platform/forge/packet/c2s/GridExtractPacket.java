package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.GridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class GridExtractPacket<T> {
    private final PlatformStorageChannelType<T> storageChannelType;
    private final ResourceLocation storageChannelTypeId;
    private final T resource;
    private final GridExtractMode mode;
    private final boolean cursor;

    public GridExtractPacket(
        final PlatformStorageChannelType<T> storageChannelType,
        final ResourceLocation storageChannelTypeId,
        final T resource,
        final GridExtractMode mode,
        final boolean cursor
    ) {
        this.storageChannelType = storageChannelType;
        this.storageChannelTypeId = storageChannelTypeId;
        this.resource = resource;
        this.mode = mode;
        this.cursor = cursor;
    }

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

    public static <T> void encode(final GridExtractPacket<T> packet, final FriendlyByteBuf buf) {
        buf.writeResourceLocation(packet.storageChannelTypeId);
        writeMode(buf, packet.mode);
        buf.writeBoolean(packet.cursor);
        packet.storageChannelType.toBuffer(packet.resource, buf);
    }

    public static <T> void handle(final GridExtractPacket<T> packet, final Supplier<NetworkEvent.Context> ctx) {
        final ServerPlayer player = ctx.get().getSender();
        if (player != null && player.containerMenu instanceof GridExtractionStrategy strategy) {
            ctx.get().enqueueWork(() -> strategy.onExtract(
                packet.storageChannelType,
                packet.resource,
                packet.mode,
                packet.cursor
            ));
        }
        ctx.get().setPacketHandled(true);
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
}
