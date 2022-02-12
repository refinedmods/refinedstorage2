package com.refinedmods.refinedstorage2.platform.abstractions;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

public interface WrenchHelper {
    boolean isWrench(Item item);

    boolean isWrenchable(BlockState state);
}
