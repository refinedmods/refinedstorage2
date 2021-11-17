package com.refinedmods.refinedstorage2.platform.fabric.item.block;

import com.refinedmods.refinedstorage2.platform.fabric.init.ColorMap;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ColoredBlockItem extends BlockItem {
    private final Component displayName;

    public ColoredBlockItem(Block block, Properties properties, DyeColor color, Component displayName) {
        super(block, properties);
        if (color != ColorMap.NORMAL_COLOR) {
            this.displayName = new TranslatableComponent("color.minecraft." + color.getName()).append(" ").append(displayName);
        } else {
            this.displayName = displayName;
        }
    }

    @Override
    public Component getDescription() {
        return displayName;
    }

    @Override
    public Component getName(ItemStack stack) {
        return displayName;
    }
}
