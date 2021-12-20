package com.refinedmods.refinedstorage2.platform.fabric.item.block;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class NameableBlockItem extends BlockItem {
    private final Component name;

    public NameableBlockItem(Block block, Properties properties, DyeColor color, Component name) {
        super(block, properties);
        this.name = name;
    }

    @Override
    public Component getDescription() {
        return name;
    }

    @Override
    public Component getName(ItemStack stack) {
        return name;
    }
}
