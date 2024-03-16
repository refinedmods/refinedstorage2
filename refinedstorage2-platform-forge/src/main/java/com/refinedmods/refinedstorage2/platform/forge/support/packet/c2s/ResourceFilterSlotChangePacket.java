package com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s;

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

import static java.util.Objects.requireNonNull;

public record ResourceFilterSlotChangePacket(
    int slotIndex,
    @Nullable
    PlatformResourceKey resource,
    @Nullable
    ResourceType resourceType,
    @Nullable
    ResourceLocation resourceTypeId
) implements CustomPacketPayload {
    public static ResourceFilterSlotChangePacket decode(final FriendlyByteBuf buf) {
        final int slotIndex = buf.readInt();
        final ResourceLocation resourceTypeId = buf.readResourceLocation();
        return PlatformApi.INSTANCE.getResourceTypeRegistry().get(resourceTypeId)
            .map(resourceType -> decode(buf, slotIndex, resourceType, resourceTypeId))
            .orElseGet(() -> new ResourceFilterSlotChangePacket(slotIndex, null, null, resourceTypeId));
    }

    private static ResourceFilterSlotChangePacket decode(final FriendlyByteBuf buf,
                                                         final int slotIndex,
                                                         final ResourceType type,
                                                         final ResourceLocation typeId) {
        final PlatformResourceKey resource = type.fromBuffer(buf);
        return new ResourceFilterSlotChangePacket(slotIndex, resource, type, typeId);
    }

    public static void handle(final ResourceFilterSlotChangePacket packet,
                              final PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            if (player.containerMenu instanceof AbstractResourceContainerMenu containerMenu) {
                containerMenu.handleResourceFilterSlotUpdate(packet.slotIndex, requireNonNull(packet.resource));
            }
        }));
    }

    @Override
    public void write(final FriendlyByteBuf buf) {
        buf.writeInt(slotIndex);
        buf.writeResourceLocation(requireNonNull(resourceTypeId));
        requireNonNull(resource).toBuffer(buf);
    }

    @Override
    public ResourceLocation id() {
        return PacketIds.RESOURCE_FILTER_SLOT_CHANGE;
    }
}
