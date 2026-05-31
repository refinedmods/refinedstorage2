package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record GridAutocraftingTaskRemovedPacket(TaskId taskId) implements CustomPacketPayload {
    public static final Type<GridAutocraftingTaskRemovedPacket> PACKET_TYPE =
        new Type<>(createIdentifier("grid_autocrafting_task_removed"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GridAutocraftingTaskRemovedPacket> STREAM_CODEC =
        StreamCodec.composite(
            UUIDUtil.STREAM_CODEC.map(TaskId::new, TaskId::id), GridAutocraftingTaskRemovedPacket::taskId,
            GridAutocraftingTaskRemovedPacket::new
        );

    public static void handle(final GridAutocraftingTaskRemovedPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractGridContainerMenu containerMenu) {
            containerMenu.taskRemoved(packet.taskId);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
