package com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;

import java.util.Objects;
import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record ResourceFilterSlotChangePacket<T>(
    int slotIndex,
    @Nullable
    T resource,
    @Nullable
    PlatformStorageChannelType<T> storageChannelType,
    @Nullable
    ResourceLocation storageChannelTypeId
) implements CustomPacketPayload {
    public static ResourceFilterSlotChangePacket<?> decode(final FriendlyByteBuf buf) {
        final int slotIndex = buf.readInt();
        final ResourceLocation storageChannelTypeId = buf.readResourceLocation();
        return PlatformApi.INSTANCE.getStorageChannelTypeRegistry().get(storageChannelTypeId)
            .map(storageChannelType -> decode(buf, slotIndex, storageChannelType, storageChannelTypeId))
            .orElseGet(() -> new ResourceFilterSlotChangePacket<>(slotIndex, null, null, storageChannelTypeId));
    }

    private static <T> ResourceFilterSlotChangePacket<T> decode(final FriendlyByteBuf buf,
                                                                final int slotIndex,
                                                                final PlatformStorageChannelType<T> type,
                                                                final ResourceLocation typeId) {
        final T resource = type.fromBuffer(buf);
        return new ResourceFilterSlotChangePacket<>(slotIndex, resource, type, typeId);
    }

    public static <T> void handle(final ResourceFilterSlotChangePacket<T> packet,
                                  final PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            if (player.containerMenu instanceof AbstractResourceContainerMenu containerMenu) {
                containerMenu.handleResourceFilterSlotUpdate(
                    packet.slotIndex,
                    Objects.requireNonNull(packet.storageChannelType),
                    Objects.requireNonNull(packet.resource)
                );
            }
        }));
    }

    @Override
    public void write(final FriendlyByteBuf buf) {
        buf.writeInt(slotIndex);
        buf.writeResourceLocation(Objects.requireNonNull(storageChannelTypeId));
        Objects.requireNonNull(storageChannelType).toBuffer(Objects.requireNonNull(resource), buf);
    }

    @Override
    public ResourceLocation id() {
        return PacketIds.RESOURCE_FILTER_SLOT_CHANGE;
    }
}
