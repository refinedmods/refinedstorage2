package com.refinedmods.refinedstorage2.platform.common.item.block;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public abstract class NameableBlockItem extends BlockItem {
    private final Component name;

    protected NameableBlockItem(final Block block, final Properties properties, final Component name) {
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
