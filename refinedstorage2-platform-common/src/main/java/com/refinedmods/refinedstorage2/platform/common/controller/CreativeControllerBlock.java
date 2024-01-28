package com.refinedmods.refinedstorage2.platform.common.controller;

import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;

public class CreativeControllerBlock extends AbstractControllerBlock<CreativeControllerBlockItem> {
    public CreativeControllerBlock(final MutableComponent name,
                                   final ControllerBlockEntityTicker ticker,
                                   final DyeColor color) {
        super(ControllerType.CREATIVE, name, ticker, color);
    }

    @Override
    public BlockColorMap<
        AbstractControllerBlock<CreativeControllerBlockItem>,
        CreativeControllerBlockItem> getBlockColorMap() {
        return Blocks.INSTANCE.getCreativeController();
    }

    @Override
    public CreativeControllerBlockItem createBlockItem() {
        return new CreativeControllerBlockItem(this, name);
    }
}
