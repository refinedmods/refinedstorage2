package com.refinedmods.refinedstorage2.platform.forge.internal;

import com.refinedmods.refinedstorage2.platform.abstractions.WrenchHelper;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

public class WrenchHelperImpl implements WrenchHelper {
    @Override
    public boolean isWrench(Item item) {
        return false;
    }

    @Override
    public boolean isWrenchable(BlockState state) {
        return false;
    }
}
