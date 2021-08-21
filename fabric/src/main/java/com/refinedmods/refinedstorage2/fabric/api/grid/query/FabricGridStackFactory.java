package com.refinedmods.refinedstorage2.fabric.api.grid.query;

import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.grid.GridStack;
import com.refinedmods.refinedstorage2.fabric.api.grid.FabricItemGridStack;
import com.refinedmods.refinedstorage2.fabric.util.ItemStacks;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.item.Item;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FabricGridStackFactory implements Function<Rs2ItemStack, GridStack<Rs2ItemStack>> {
    @Override
    public GridStack<Rs2ItemStack> apply(Rs2ItemStack stack) {
        Item item = ItemStacks.toItem(stack.getItem());

        String name = stack.getName();
        String modId = Registry.ITEM.getId(item).getNamespace();
        String modName = FabricLoader.getInstance().getModContainer(modId).map(ModContainer::getMetadata).map(ModMetadata::getName).orElse("");
        Set<String> tags = ItemTags.getTagGroup().getTagsFor(item).stream().map(Identifier::getPath).collect(Collectors.toSet());

        return new FabricItemGridStack(stack, name, modId, modName, tags);
    }
}
