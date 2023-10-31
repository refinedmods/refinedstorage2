package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.item.block.NamedBlockItem;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public abstract class AbstractColoredBlock<T extends Block & BlockItemProvider>
    extends AbstractBaseBlock implements ColorableBlock<T>, BlockItemProvider {
    private final DyeColor color;
    private final MutableComponent name;

    protected AbstractColoredBlock(final Properties properties, final DyeColor color, final MutableComponent name) {
        super(properties);
        this.color = color;
        this.name = name;
    }

    @Override
    public MutableComponent getName() {
        return name;
    }

    @Override
    public DyeColor getColor() {
        return color;
    }

    @Override
    public BlockItem createBlockItem() {
        return new NamedBlockItem(this, new Item.Properties(), name);
    }
}
