package com.refinedmods.refinedstorage2.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

public class PropertyChangePacket implements PacketConsumer {
    public static final Identifier ID = new Identifier(RefinedStorage2Mod.ID, "property_change");

    @Override
    public void accept(PacketContext context, PacketByteBuf buffer) {
        int id = buffer.readInt();
        int value = buffer.readInt();

        context.getTaskQueue().execute(() -> {
            ScreenHandler screenHandler = context.getPlayer().currentScreenHandler;
            if (screenHandler != null) {
                screenHandler.setProperty(id, value);
            }
        });
    }
}
