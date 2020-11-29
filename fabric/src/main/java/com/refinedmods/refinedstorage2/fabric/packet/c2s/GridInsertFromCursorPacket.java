package com.refinedmods.refinedstorage2.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.screen.grid.GridEventHandler;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

public class GridInsertFromCursorPacket implements PacketConsumer {
    public static final Identifier ID = new Identifier(RefinedStorage2Mod.ID, "grid_insert_from_cursor");

    @Override
    public void accept(PacketContext packetContext, PacketByteBuf buf) {
        boolean single = buf.readBoolean();

        packetContext.getTaskQueue().execute(() -> {
            ScreenHandler handler = packetContext.getPlayer().currentScreenHandler;
            if (handler instanceof GridEventHandler) {
                ((GridEventHandler) handler).onInsertFromCursor(single);
            }
        });
    }
}
