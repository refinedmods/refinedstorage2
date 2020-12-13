package com.refinedmods.refinedstorage2.core.grid;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Set;

public interface GridEventHandler {
    void onInsertFromCursor(boolean single);

    void onExtract(ServerPlayerEntity player, ItemStack stack, Set<GridExtractOption> options);

    void onItemUpdate(ItemStack template, int amount);
}
