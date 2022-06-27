package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view;

import com.refinedmods.refinedstorage2.platform.apiimpl.grid.view.ItemGridResourceFactory;

import java.util.Optional;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;

public class FabricItemGridResourceFactory extends ItemGridResourceFactory {
    @Override
    public Optional<String> getModName(final String modId) {
        return FabricLoader
                .getInstance()
                .getModContainer(modId)
                .map(ModContainer::getMetadata)
                .map(ModMetadata::getName);
    }

    @Override
    public String getModId(final ItemStack itemStack) {
        return Registry.ITEM.getKey(itemStack.getItem()).getNamespace();
    }
}
