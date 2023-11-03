package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.AbstractResourceContainerMenu;

import java.util.Objects;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class ResourceFilterSlotChangePacket<T> {
    private final int slotIndex;
    @Nullable
    private final T resource;
    @Nullable
    private final PlatformStorageChannelType<T> storageChannelType;
    @Nullable
    private final ResourceLocation storageChannelTypeId;

    public ResourceFilterSlotChangePacket(final int slotIndex,
                                          @Nullable final T resource,
                                          @Nullable final PlatformStorageChannelType<T> storageChannelType,
                                          @Nullable final ResourceLocation storageChannelTypeId) {
        this.slotIndex = slotIndex;
        this.resource = resource;
        this.storageChannelType = storageChannelType;
        this.storageChannelTypeId = storageChannelTypeId;
    }

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

    public static <T> void encode(final ResourceFilterSlotChangePacket<T> packet, final FriendlyByteBuf buf) {
        buf.writeInt(packet.slotIndex);
        buf.writeResourceLocation(Objects.requireNonNull(packet.storageChannelTypeId));
        Objects.requireNonNull(packet.storageChannelType).toBuffer(Objects.requireNonNull(packet.resource), buf);
    }

    public static <T> void handle(final ResourceFilterSlotChangePacket<T> packet,
                                  final Supplier<NetworkEvent.Context> ctx) {
        final ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            ctx.get().enqueueWork(() -> handle(player, packet));
        }
        ctx.get().setPacketHandled(true);
    }

    private static <T> void handle(final Player player, final ResourceFilterSlotChangePacket<T> packet) {
        final AbstractContainerMenu menu = player.containerMenu;
        if (menu instanceof AbstractResourceContainerMenu containerMenu) {
            containerMenu.handleResourceFilterSlotUpdate(
                packet.slotIndex,
                Objects.requireNonNull(packet.storageChannelType),
                Objects.requireNonNull(packet.resource)
            );
        }
    }
}
