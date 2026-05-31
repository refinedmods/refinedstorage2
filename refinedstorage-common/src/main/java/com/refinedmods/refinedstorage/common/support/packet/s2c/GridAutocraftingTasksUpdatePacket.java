package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.common.autocrafting.monitor.AutocraftingMonitorStreamCodecs;
import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public record GridAutocraftingTasksUpdatePacket(List<TaskStatus> statuses) implements CustomPacketPayload {
    public static final Type<GridAutocraftingTasksUpdatePacket> PACKET_TYPE = new Type<>(
        createIdentifier("grid_autocrafting_tasks_update")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, GridAutocraftingTasksUpdatePacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, AutocraftingMonitorStreamCodecs.STATUS_STREAM_CODEC),
            GridAutocraftingTasksUpdatePacket::statuses,
            GridAutocraftingTasksUpdatePacket::new
        );

    public static void handle(final GridAutocraftingTasksUpdatePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractGridContainerMenu containerMenu) {
            packet.statuses.forEach(containerMenu::taskStatusChanged);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
