package com.refinedmods.refinedstorage.platform.fabric.grid.strategy;

import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridAllowExtractionStrategy;
import com.refinedmods.refinedstorage.platform.api.grid.view.PlatformGridResource;

import net.minecraft.world.item.ItemStack;

public class ItemGridAllowExtractionStrategy implements GridAllowExtractionStrategy {
    @Override
    public boolean allowExtraction(final PlatformGridResource resource, final ItemStack carriedStack) {
        return carriedStack.isEmpty();
    }
}
