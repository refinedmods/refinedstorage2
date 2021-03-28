package com.refinedmods.refinedstorage2.fabric.packet.s2c;

import com.refinedmods.refinedstorage2.core.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

public class GridActivePacket implements PacketConsumer {
    public static final Identifier ID = new Identifier(RefinedStorage2Mod.ID, "grid_active");

    @Override
    public void accept(PacketContext packetContext, PacketByteBuf buf) {
        boolean active = buf.readBoolean();

        packetContext.getTaskQueue().execute(() -> {
            ScreenHandler handler = packetContext.getPlayer().currentScreenHandler;
            if (handler instanceof GridEventHandler) {
                ((GridEventHandler) handler).onActiveChanged(active);
            }
        });
    }
}
