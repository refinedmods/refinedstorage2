package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record GridAutocraftingTaskAddedPacket(
    PlatformResourceKey resource,
    TaskId taskId
) implements CustomPacketPayload {
    public static final Type<GridAutocraftingTaskAddedPacket> PACKET_TYPE =
        new Type<>(createIdentifier("grid_autocrafting_task_added"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GridAutocraftingTaskAddedPacket> STREAM_CODEC =
        StreamCodec.composite(
            ResourceCodecs.STREAM_CODEC, GridAutocraftingTaskAddedPacket::resource,
            UUIDUtil.STREAM_CODEC.map(TaskId::new, TaskId::id), GridAutocraftingTaskAddedPacket::taskId,
            GridAutocraftingTaskAddedPacket::new
        );

    public static void handle(final GridAutocraftingTaskAddedPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractGridContainerMenu containerMenu) {
            containerMenu.taskAdded(packet.resource, packet.taskId);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
