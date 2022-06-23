package com.refinedmods.refinedstorage2.platform.api.grid;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

public interface GridSynchronizerRegistry {
    void register(ResourceLocation id, GridSynchronizer synchronizer);

    boolean hasSynchronizers();

    ResourceLocation getId(GridSynchronizer synchronizer);

    GridSynchronizer getOrDefault(ResourceLocation id);

    GridSynchronizer getDefault();

    GridSynchronizer toggleSynchronizer(GridSynchronizer synchronizer);

    List<GridSynchronizer> getAll();
}
