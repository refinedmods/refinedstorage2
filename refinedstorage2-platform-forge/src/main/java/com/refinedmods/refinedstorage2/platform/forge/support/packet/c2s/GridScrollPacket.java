package com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record GridScrollPacket(
    ResourceType resourceType,
    ResourceLocation resourceTypeId,
    PlatformResourceKey resource,
    GridScrollMode mode,
    int slotIndex
) implements CustomPacketPayload {
    public static GridScrollPacket decode(final FriendlyByteBuf buf) {
        final ResourceLocation resourceTypeId = buf.readResourceLocation();
        final ResourceType resourceType = PlatformApi.INSTANCE
            .getResourceTypeRegistry()
            .get(resourceTypeId)
            .orElseThrow();
        final GridScrollMode mode = getMode(buf.readByte());
        final int slotIndex = buf.readInt();
        final PlatformResourceKey resource = resourceType.fromBuffer(buf);
        return new GridScrollPacket(
            resourceType,
            resourceTypeId,
            resource,
            mode,
            slotIndex
        );
    }

    public static void handle(final GridScrollPacket packet, final PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            if (player.containerMenu instanceof GridScrollingStrategy strategy) {
                strategy.onScroll(packet.resource, packet.mode, packet.slotIndex);
            }
        }));
    }

    private static GridScrollMode getMode(final byte mode) {
        if (mode == 0) {
            return GridScrollMode.GRID_TO_INVENTORY;
        } else if (mode == 1) {
            return GridScrollMode.GRID_TO_CURSOR;
        }
        return GridScrollMode.INVENTORY_TO_GRID;
    }

    private static void writeMode(final FriendlyByteBuf buf, final GridScrollMode mode) {
        switch (mode) {
            case GRID_TO_INVENTORY -> buf.writeByte(0);
            case GRID_TO_CURSOR -> buf.writeByte(1);
            case INVENTORY_TO_GRID -> buf.writeByte(2);
        }
    }

    @Override
    public void write(final FriendlyByteBuf buf) {
        buf.writeResourceLocation(resourceTypeId);
        writeMode(buf, mode);
        buf.writeInt(slotIndex);
        resource.toBuffer(buf);
    }

    @Override
    public ResourceLocation id() {
        return PacketIds.GRID_SCROLL;
    }
}
