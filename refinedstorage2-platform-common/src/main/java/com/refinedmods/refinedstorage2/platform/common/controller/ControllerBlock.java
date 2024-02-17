package com.refinedmods.refinedstorage2.platform.common.controller;

import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;

public class ControllerBlock extends AbstractControllerBlock<ControllerBlockItem> {
    public ControllerBlock(final MutableComponent name,
                           final ControllerBlockEntityTicker ticker,
                           final DyeColor color) {
        super(ControllerType.NORMAL, name, ticker, color);
    }

    @Override
    public BlockColorMap<AbstractControllerBlock<ControllerBlockItem>, ControllerBlockItem> getBlockColorMap() {
        return Blocks.INSTANCE.getController();
    }

    @Override
    public ControllerBlockItem createBlockItem() {
        return new ControllerBlockItem(this, name);
    }
}
