package com.refinedmods.refinedstorage2.platform.forge.support.packet.s2c;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record ResourceSlotUpdatePacket(
    int slotIndex,
    @Nullable ResourceAmount resourceAmount,
    @Nullable ResourceLocation resourceTypeId
) implements CustomPacketPayload {
    public static ResourceSlotUpdatePacket decode(final FriendlyByteBuf buf) {
        final int slotIndex = buf.readInt();
        final boolean present = buf.readBoolean();
        if (!present) {
            return new ResourceSlotUpdatePacket(slotIndex, null, null);
        }
        final ResourceLocation resourceTypeId = buf.readResourceLocation();
        return PlatformApi.INSTANCE.getResourceTypeRegistry().get(resourceTypeId).map(
            resourceType -> decode(buf, slotIndex, resourceType)
        ).orElseGet(() -> new ResourceSlotUpdatePacket(slotIndex, null, null));
    }

    private static ResourceSlotUpdatePacket decode(final FriendlyByteBuf buf,
                                                   final int slotIndex,
                                                   final ResourceType type) {
        final PlatformResourceKey resource = type.fromBuffer(buf);
        final long amount = buf.readLong();
        return new ResourceSlotUpdatePacket(slotIndex, new ResourceAmount(resource, amount), null);
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
        final boolean present = resourceAmount != null && resourceTypeId != null;
        buf.writeBoolean(present);
        if (present) {
            buf.writeResourceLocation(resourceTypeId);
            ((PlatformResourceKey) resourceAmount.getResource()).toBuffer(buf);
            buf.writeLong(resourceAmount.getAmount());
        }
    }

    @Override
    public ResourceLocation id() {
        return PacketIds.RESOURCE_SLOT_UPDATE;
    }
}
