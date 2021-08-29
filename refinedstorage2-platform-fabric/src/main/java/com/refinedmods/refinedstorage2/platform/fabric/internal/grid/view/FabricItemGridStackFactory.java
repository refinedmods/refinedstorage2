package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.stack.GridStack;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.stack.FabricItemGridStack;

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

public class FabricItemGridStackFactory implements Function<Rs2ItemStack, GridStack<Rs2ItemStack>> {
    @Override
    public GridStack<Rs2ItemStack> apply(Rs2ItemStack stack) {
        Item item = Rs2PlatformApiFacade.INSTANCE.toMcItem(stack.getItem());

        String name = item.getName().getString();
        String modId = Registry.ITEM.getId(item).getNamespace();
        String modName = FabricLoader.getInstance().getModContainer(modId).map(ModContainer::getMetadata).map(ModMetadata::getName).orElse("");
        Set<String> tags = ItemTags.getTagGroup().getTagsFor(item).stream().map(Identifier::getPath).collect(Collectors.toSet());

        return new FabricItemGridStack(stack, name, modId, modName, tags);
    }
}
