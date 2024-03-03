package com.refinedmods.refinedstorage2.platform.forge.support.packet.s2c;

import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record ResourceSlotUpdatePacket(
    int slotIndex,
    @Nullable ResourceAmountTemplate resourceAmount,
    @Nullable ResourceLocation storageChannelTypeId
)
    implements CustomPacketPayload {
    public static ResourceSlotUpdatePacket decode(final FriendlyByteBuf buf) {
        final int slotIndex = buf.readInt();
        final boolean present = buf.readBoolean();
        if (!present) {
            return new ResourceSlotUpdatePacket(slotIndex, null, null);
        }
        final ResourceLocation storageChannelTypeId = buf.readResourceLocation();
        return PlatformApi.INSTANCE.getStorageChannelTypeRegistry().get(storageChannelTypeId).map(
            storageChannelType -> decode(buf, slotIndex, storageChannelType)
        ).orElseGet(() -> new ResourceSlotUpdatePacket(slotIndex, null, null));
    }

    private static ResourceSlotUpdatePacket decode(final FriendlyByteBuf buf,
                                                   final int slotIndex,
                                                   final PlatformStorageChannelType type) {
        final ResourceKey resource = type.fromBuffer(buf);
        final long amount = buf.readLong();
        final ResourceAmountTemplate resourceAmount = new ResourceAmountTemplate(resource, amount, type);
        return new ResourceSlotUpdatePacket(slotIndex, resourceAmount, null);
    }

    public static void handle(final ResourceSlotUpdatePacket packet,
                              final PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            if (player.containerMenu instanceof AbstractResourceContainerMenu containerMenu) {
                containerMenu.handleResourceSlotUpdate(packet.slotIndex, packet.resourceAmount);
            }
        }));
    }

    @Override
    public void write(final FriendlyByteBuf buf) {
        buf.writeInt(slotIndex);
        final boolean present = resourceAmount != null && storageChannelTypeId != null;
        buf.writeBoolean(present);
        if (present) {
            buf.writeResourceLocation(storageChannelTypeId);
            resourceAmount.getStorageChannelType().toBuffer(resourceAmount.getResource(), buf);
            buf.writeLong(resourceAmount.getAmount());
        }
    }

    @Override
    public ResourceLocation id() {
        return PacketIds.RESOURCE_SLOT_UPDATE;
    }
}
