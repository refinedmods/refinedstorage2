package com.refinedmods.refinedstorage.common.support.packet.c2s;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.common.autocrafting.monitor.AutocraftingMonitorStreamCodecs;
import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record GridAutocraftingTasksSubscriptionPacket(Set<TaskId> taskIds) implements CustomPacketPayload {
    public static final Type<GridAutocraftingTasksSubscriptionPacket> PACKET_TYPE = new Type<>(
        createIdentifier("grid_autocrafting_tasks_subscription")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, GridAutocraftingTasksSubscriptionPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.collection(HashSet::new, AutocraftingMonitorStreamCodecs.TASK_ID_STREAM_CODEC),
            GridAutocraftingTasksSubscriptionPacket::taskIds,
            GridAutocraftingTasksSubscriptionPacket::new
        );

    public static void handle(final GridAutocraftingTasksSubscriptionPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractGridContainerMenu containerMenu) {
            containerMenu.setSubscribedAutocraftingTaskIds(packet.taskIds());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
