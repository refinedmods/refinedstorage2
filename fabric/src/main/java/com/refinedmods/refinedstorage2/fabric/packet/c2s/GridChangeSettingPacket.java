package com.refinedmods.refinedstorage2.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.core.grid.GridSortingDirection;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.screen.handler.grid.GridScreenHandler;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

public class GridChangeSettingPacket implements PacketConsumer {
    public static final Identifier ID = new Identifier(RefinedStorage2Mod.ID, "grid_setting_change");

    @Override
    public void accept(PacketContext packetContext, PacketByteBuf buf) {
        byte type = buf.readByte();
        int value = buf.readInt();

        if (type == 0) {
            handleSortingDirectionChange(packetContext, value);
        }
    }

    private void handleSortingDirectionChange(PacketContext packetContext, int value) {
        packetContext.getTaskQueue().execute(() -> {
            ScreenHandler screenHandler = packetContext.getPlayer().currentScreenHandler;
            if (screenHandler instanceof GridScreenHandler) {
                ((GridScreenHandler) screenHandler).getGrid().setSortingDirection(value == 0 ? GridSortingDirection.ASCENDING : GridSortingDirection.DESCENDING);
            }
        });
    }

    public static void writeSortingDirection(PacketByteBuf buf, GridSortingDirection sortingDirection) {
        buf.writeByte(0);
        buf.writeInt(sortingDirection == GridSortingDirection.ASCENDING ? 0 : 1);
    }
}
