package com.refinedmods.refinedstorage2.platform.forge.packet.s2c;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractResourceFilterContainerMenu;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class ResourceFilterSlotUpdatePacket<T> {
    private final int slotIndex;
    @Nullable
    private final PlatformStorageChannelType<T> storageChannelType;
    @Nullable
    private final ResourceLocation storageChannelTypeId;
    @Nullable
    private final T resource;
    private final long amount;

    public ResourceFilterSlotUpdatePacket(final int slotIndex,
                                          @Nullable final PlatformStorageChannelType<T> storageChannelType,
                                          @Nullable final ResourceLocation storageChannelTypeId,
                                          @Nullable final T resource,
                                          final long amount) {
        this.slotIndex = slotIndex;
        this.storageChannelType = storageChannelType;
        this.storageChannelTypeId = storageChannelTypeId;
        this.resource = resource;
        this.amount = amount;
    }

    public static ResourceFilterSlotUpdatePacket<?> decode(final FriendlyByteBuf buf) {
        final int slotIndex = buf.readInt();
        final boolean present = buf.readBoolean();
        if (!present) {
            return new ResourceFilterSlotUpdatePacket<>(slotIndex, null, null, null, 0);
        }
        final ResourceLocation storageChannelTypeId = buf.readResourceLocation();
        return PlatformApi.INSTANCE.getStorageChannelTypeRegistry().get(storageChannelTypeId).map(
            storageChannelType -> decode(buf, slotIndex, storageChannelTypeId, storageChannelType)
        ).orElseGet(() -> new ResourceFilterSlotUpdatePacket<>(slotIndex, null, null, null, 0));
    }

    private static <T> ResourceFilterSlotUpdatePacket<T> decode(final FriendlyByteBuf buf,
                                                                final int slotIndex,
                                                                final ResourceLocation storageChannelTypeId,
                                                                final PlatformStorageChannelType<T> type) {
        final T resource = type.fromBuffer(buf);
        final long amount = buf.readLong();
        return new ResourceFilterSlotUpdatePacket<>(
            slotIndex,
            type,
            storageChannelTypeId,
            resource,
            amount
        );
    }

    public static <T> void encode(final ResourceFilterSlotUpdatePacket<T> packet, final FriendlyByteBuf buf) {
        buf.writeInt(packet.slotIndex);
        final boolean present = packet.storageChannelType != null
            && packet.storageChannelTypeId != null
            && packet.resource != null;
        buf.writeBoolean(present);
        if (present) {
            buf.writeResourceLocation(packet.storageChannelTypeId);
            packet.storageChannelType.toBuffer(packet.resource, buf);
            buf.writeLong(packet.amount);
        }
    }

    public static <T> void handle(final ResourceFilterSlotUpdatePacket<T> packet,
                                  final Supplier<NetworkEvent.Context> ctx) {
        ClientProxy.getPlayer().ifPresent(player -> handle(player, packet));
        ctx.get().setPacketHandled(true);
    }

    private static <T> void handle(final Player player, final ResourceFilterSlotUpdatePacket<T> packet) {
        final AbstractContainerMenu menu = player.containerMenu;
        if (menu instanceof AbstractResourceFilterContainerMenu containerMenu) {
            if (packet.storageChannelType != null && packet.resource != null) {
                packet.storageChannelType.toFilteredResource(new ResourceAmount<>(
                    packet.resource,
                    packet.amount
                )).ifPresent(filteredResource -> containerMenu.handleResourceFilterSlotUpdate(
                    packet.slotIndex,
                    filteredResource
                ));
            } else {
                containerMenu.handleResourceFilterSlotUpdate(packet.slotIndex, null);
            }
        }
    }
}
