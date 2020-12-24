package com.refinedmods.refinedstorage2.fabric.coreimpl.grid.query;

import com.refinedmods.refinedstorage2.core.grid.GridStack;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FabricGridStackFactory implements Function<ItemStack, GridStack<ItemStack>> {
    @Override
    public GridStack<ItemStack> apply(ItemStack stack) {
        String name = stack.getName().getString();
        String modId = Registry.ITEM.getId(stack.getItem()).getNamespace();
        String modName = FabricLoader.getInstance().getModContainer(modId).map(ModContainer::getMetadata).map(ModMetadata::getName).orElse("");
        Set<String> tags = ItemTags.getTagGroup().getTagsFor(stack.getItem()).stream().map(Identifier::getPath).collect(Collectors.toSet());

        return new GridStack<>(stack, name, modId, modName, tags);
    }
}
