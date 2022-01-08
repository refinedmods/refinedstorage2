package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view;

import com.refinedmods.refinedstorage2.platform.common.internal.grid.view.ItemGridResourceFactory;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

public class FabricItemGridResourceFactory extends ItemGridResourceFactory {
    @Override
    public String getModName(String modId) {
        return FabricLoader
                .getInstance()
                .getModContainer(modId)
                .map(ModContainer::getMetadata)
                .map(ModMetadata::getName)
                .orElse("");
    }
}
