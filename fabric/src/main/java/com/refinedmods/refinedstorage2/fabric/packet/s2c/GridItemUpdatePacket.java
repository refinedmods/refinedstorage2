package com.refinedmods.refinedstorage2.fabric.packet.s2c;

import com.refinedmods.refinedstorage2.core.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.core.storage.StorageTracker;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.util.PacketUtil;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

public class GridItemUpdatePacket implements PacketConsumer {
    public static final Identifier ID = new Identifier(RefinedStorage2Mod.ID, "grid_item_update");

    @Override
    public void accept(PacketContext packetContext, PacketByteBuf buf) {
        ItemStack template = PacketUtil.readItemStackWithoutCount(buf);
        int amount = buf.readInt();
        StorageTracker.Entry trackerEntry = PacketUtil.readTrackerEntry(buf);

        packetContext.getTaskQueue().execute(() -> {
            ScreenHandler handler = packetContext.getPlayer().currentScreenHandler;
            if (handler instanceof GridEventHandler) {
                ((GridEventHandler) handler).onItemUpdate(template, amount, trackerEntry);
            }
        });
    }
}
