package com.refinedmods.refinedstorage2.platform.common.block.entity.grid;

import javax.annotation.Nullable;

import net.minecraft.world.inventory.TransientCraftingContainer;

public class CraftingMatrix extends TransientCraftingContainer {
    @Nullable
    private final Runnable listener;

    public CraftingMatrix(@Nullable final Runnable listener) {
        super(new CraftingMatrixContainerMenu(listener), 3, 3);
        this.listener = listener;
    }

    public void changed() {
        if (listener != null) {
            listener.run();
        }
    }
}
