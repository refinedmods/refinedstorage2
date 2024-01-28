package com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s;

import com.refinedmods.refinedstorage2.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record GridInsertPacket(boolean single, boolean tryAlternatives) implements CustomPacketPayload {
    public static GridInsertPacket decode(final FriendlyByteBuf buf) {
        return new GridInsertPacket(buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(final GridInsertPacket packet, final PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            if (player.containerMenu instanceof GridInsertionStrategy strategy) {
                final GridInsertMode mode = packet.single
                    ? GridInsertMode.SINGLE_RESOURCE
                    : GridInsertMode.ENTIRE_RESOURCE;
                strategy.onInsert(mode, packet.tryAlternatives);
            }
        }));
    }

    @Override
    public void write(final FriendlyByteBuf buf) {
        buf.writeBoolean(single);
        buf.writeBoolean(tryAlternatives);
    }

    @Override
    public ResourceLocation id() {
        return PacketIds.GRID_INSERT;
    }
}
