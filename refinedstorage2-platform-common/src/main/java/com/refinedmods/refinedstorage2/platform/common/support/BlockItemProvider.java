package com.refinedmods.refinedstorage2.platform.common.support;

import net.minecraft.world.item.BlockItem;

@FunctionalInterface
public interface BlockItemProvider {
    BlockItem createBlockItem();
}
