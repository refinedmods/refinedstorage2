package com.refinedmods.refinedstorage2.core.grid;

import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.function.Function;

public class FakeGridStackFactory implements Function<ItemStack, GridStack<ItemStack>> {
    @Override
    public GridStack<ItemStack> apply(ItemStack stack) {
        return new ItemGridStack(
            stack,
            stack.getName().getString(),
            "mc",
            "Minecraft",
            Collections.emptySet()
        );
    }
}
