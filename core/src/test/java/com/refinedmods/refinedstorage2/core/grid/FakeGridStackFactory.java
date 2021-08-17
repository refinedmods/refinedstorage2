package com.refinedmods.refinedstorage2.core.grid;

import com.refinedmods.refinedstorage2.core.stack.item.Rs2ItemStack;

import java.util.Collections;
import java.util.function.Function;

public class FakeGridStackFactory implements Function<Rs2ItemStack, GridStack<Rs2ItemStack>> {
    @Override
    public GridStack<Rs2ItemStack> apply(Rs2ItemStack stack) {
        return new ItemGridStack(
                stack,
                stack.getName(),
                "mc",
                "Minecraft",
                Collections.emptySet()
        );
    }
}
