package com.refinedmods.refinedstorage.platform.api.grid.strategy;

import com.refinedmods.refinedstorage.platform.api.grid.view.PlatformGridResource;

import net.minecraft.world.item.ItemStack;

public interface GridAllowExtractionStrategy {
    boolean allowExtraction(PlatformGridResource resource, ItemStack carriedStack);
}
