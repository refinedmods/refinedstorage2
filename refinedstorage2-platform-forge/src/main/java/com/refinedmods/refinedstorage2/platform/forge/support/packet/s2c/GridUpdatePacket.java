package com.refinedmods.refinedstorage2.platform.forge.support.packet.s2c;

import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record GridUpdatePacket<T>(PlatformStorageChannelType<T> storageChannelType,
                                  ResourceLocation storageChannelTypeId,
                                  T resource,
                                  long amount,
                                  @Nullable TrackedResource trackedResource) implements CustomPacketPayload {
    @SuppressWarnings("unchecked")
    public static GridUpdatePacket<?> decode(final FriendlyByteBuf buf) {
        final ResourceLocation storageChannelTypeId = buf.readResourceLocation();
        final PlatformStorageChannelType<?> storageChannelType = PlatformApi.INSTANCE
            .getStorageChannelTypeRegistry()
            .get(storageChannelTypeId)
            .orElseThrow();
        final Object resource = storageChannelType.fromBuffer(buf);
        final long amount = buf.readLong();
        final TrackedResource trackedResource = PacketUtil.readTrackedResource(buf);
        return new GridUpdatePacket<>(
            (PlatformStorageChannelType<? super Object>) storageChannelType,
            storageChannelTypeId,
            resource,
            amount,
            trackedResource
        );
    }

    public static <T> void handle(final GridUpdatePacket<T> packet, final PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            if (player.containerMenu instanceof AbstractGridContainerMenu containerMenu) {
                containerMenu.onResourceUpdate(packet.resource, packet.amount, packet.trackedResource);
            }
        }));
    }

    @Override
    public void write(final FriendlyByteBuf buf) {
        buf.writeResourceLocation(storageChannelTypeId);
        storageChannelType.toBuffer(resource, buf);
        buf.writeLong(amount);
        PacketUtil.writeTrackedResource(buf, trackedResource);
    }

    @Override
    public ResourceLocation id() {
        return PacketIds.GRID_UPDATE;
    }
}
