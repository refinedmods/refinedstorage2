package com.refinedmods.refinedstorage2.fabric.item.block;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;

public class ColoredBlockItem extends BlockItem {
    private final Text displayName;

    public ColoredBlockItem(Block block, Settings settings, DyeColor color, Text displayName) {
        super(block, settings);
        if (color != DyeColor.LIGHT_BLUE) {
            this.displayName = new TranslatableText("color.minecraft." + color.getName()).append(" ").append(displayName);
        } else {
            this.displayName = displayName;
        }
    }

    @Override
    public Text getName() {
        return displayName;
    }

    @Override
    public Text getName(ItemStack stack) {
        return displayName;
    }
}
