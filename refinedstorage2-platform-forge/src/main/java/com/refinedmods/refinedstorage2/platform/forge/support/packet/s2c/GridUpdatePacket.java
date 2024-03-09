package com.refinedmods.refinedstorage2.platform.forge.support.packet.s2c;

import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record GridUpdatePacket(
    ResourceLocation resourceTypeId,
    PlatformResourceKey resource,
    long amount,
    @Nullable TrackedResource trackedResource
) implements CustomPacketPayload {
    public static GridUpdatePacket decode(final FriendlyByteBuf buf) {
        final ResourceLocation resourceTypeId = buf.readResourceLocation();
        final ResourceType resourceType = PlatformApi.INSTANCE
            .getResourceTypeRegistry()
            .get(resourceTypeId)
            .orElseThrow();
        final PlatformResourceKey resource = resourceType.fromBuffer(buf);
        final long amount = buf.readLong();
        final TrackedResource trackedResource = PacketUtil.readTrackedResource(buf);
        return new GridUpdatePacket(
            resourceTypeId,
            resource,
            amount,
            trackedResource
        );
    }

    public static void handle(final GridUpdatePacket packet, final PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            if (player.containerMenu instanceof AbstractGridContainerMenu containerMenu) {
                containerMenu.onResourceUpdate(packet.resource, packet.amount, packet.trackedResource);
            }
        }));
    }

    @Override
    public void write(final FriendlyByteBuf buf) {
        buf.writeResourceLocation(resourceTypeId);
        resource.toBuffer(buf);
        buf.writeLong(amount);
        PacketUtil.writeTrackedResource(buf, trackedResource);
    }

    @Override
    public ResourceLocation id() {
        return PacketIds.GRID_UPDATE;
    }
}
