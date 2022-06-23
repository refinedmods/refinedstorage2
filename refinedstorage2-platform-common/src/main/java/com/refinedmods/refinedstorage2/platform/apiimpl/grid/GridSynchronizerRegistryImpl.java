package com.refinedmods.refinedstorage2.platform.apiimpl.grid;

import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizerRegistry;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.resources.ResourceLocation;

public class GridSynchronizerRegistryImpl implements GridSynchronizerRegistry {
    private final BiMap<ResourceLocation, GridSynchronizer> registry = HashBiMap.create();
    private final List<GridSynchronizer> order = new ArrayList<>();
    private final GridSynchronizer defaultSynchronizer;

    public GridSynchronizerRegistryImpl(ResourceLocation defaultId, GridSynchronizer defaultSynchronizer) {
        this.defaultSynchronizer = defaultSynchronizer;
        register(defaultId, defaultSynchronizer);
    }

    @Override
    public void register(ResourceLocation id, GridSynchronizer synchronizer) {
        if (registry.containsKey(id) || order.contains(synchronizer)) {
            throw new IllegalArgumentException(id + " is already registered");
        }
        registry.put(id, synchronizer);
        order.add(synchronizer);
    }

    @Override
    public boolean hasSynchronizers() {
        return !registry.isEmpty();
    }

    @Override
    public ResourceLocation getId(GridSynchronizer synchronizer) {
        return registry.inverse().get(synchronizer);
    }

    @Override
    public GridSynchronizer getOrDefault(ResourceLocation id) {
        return registry.getOrDefault(id, getDefault());
    }

    @Override
    public GridSynchronizer getDefault() {
        return defaultSynchronizer;
    }

    @Override
    public GridSynchronizer toggleSynchronizer(GridSynchronizer synchronizer) {
        int index = order.indexOf(synchronizer);
        int nextIndex = index + 1;
        if (nextIndex >= order.size()) {
            return order.get(0);
        }
        return order.get(nextIndex);
    }

    @Override
    public List<GridSynchronizer> getAll() {
        return order;
    }
}
