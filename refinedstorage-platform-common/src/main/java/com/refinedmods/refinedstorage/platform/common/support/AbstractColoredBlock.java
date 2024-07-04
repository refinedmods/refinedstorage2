package com.refinedmods.refinedstorage.platform.common.support;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;

public abstract class AbstractColoredBlock<T extends Block & BlockItemProvider<BaseBlockItem>>
    extends AbstractBaseBlock implements ColorableBlock<T, BaseBlockItem>, BlockItemProvider<BaseBlockItem> {
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
}
