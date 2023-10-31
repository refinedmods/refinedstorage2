package com.refinedmods.refinedstorage2.platform.fabric.grid.view;

import com.refinedmods.refinedstorage2.platform.common.grid.view.AbstractItemGridResourceFactory;

import java.util.Optional;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public class FabricItemGridResourceFactory extends AbstractItemGridResourceFactory {
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
        return BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getNamespace();
    }
}
