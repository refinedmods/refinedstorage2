package com.refinedmods.refinedstorage2.fabric.coreimpl.grid.query;

import com.refinedmods.refinedstorage2.core.grid.query.GridStackDetails;
import com.refinedmods.refinedstorage2.core.grid.query.MemoizedGridStackDetailsProvider;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;

public class FabricGridStackDetailsProvider extends MemoizedGridStackDetailsProvider<ItemStack> {
    @Override
    protected GridStackDetails createDetails(ItemStack stack) {
        String name = stack.getName().getString();
        String modId = Registry.ITEM.getId(stack.getItem()).getNamespace();
        String modName = FabricLoader.getInstance().getModContainer(modId).map(ModContainer::getMetadata).map(ModMetadata::getName).orElse("");

        return new GridStackDetails(name, modId, modName);
    }
}
