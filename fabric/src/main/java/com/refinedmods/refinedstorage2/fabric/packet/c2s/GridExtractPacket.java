package com.refinedmods.refinedstorage2.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.core.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.core.grid.GridExtractOption;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.util.PacketUtil;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.EnumSet;
import java.util.Set;

public class GridExtractPacket implements PacketConsumer {
    public static final Identifier ID = new Identifier(RefinedStorage2Mod.ID, "grid_extract");

    @Override
    public void accept(PacketContext packetContext, PacketByteBuf buf) {
        ItemStack stack = PacketUtil.readItemStackWithoutCount(buf);

        Set<GridExtractOption> options = EnumSet.noneOf(GridExtractOption.class);
        if (buf.readBoolean()) {
            options.add(GridExtractOption.SINGLE);
        }
        if (buf.readBoolean()) {
            options.add(GridExtractOption.HALF);
        }
        if (buf.readBoolean()) {
            options.add(GridExtractOption.SHIFT);
        }

        packetContext.getTaskQueue().execute(() -> {
            ScreenHandler handler = packetContext.getPlayer().currentScreenHandler;
            if (handler instanceof GridEventHandler) {
                ((GridEventHandler) handler).onExtract((ServerPlayerEntity) packetContext.getPlayer(), stack, options);
            }
        });
    }
}
