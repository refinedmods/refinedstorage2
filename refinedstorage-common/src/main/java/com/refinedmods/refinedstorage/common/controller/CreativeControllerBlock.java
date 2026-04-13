package com.refinedmods.refinedstorage.common.controller;

import com.refinedmods.refinedstorage.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.common.content.Blocks;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

public class CreativeControllerBlock extends AbstractControllerBlock<CreativeControllerBlockItem> {
    private final Identifier id;

    public CreativeControllerBlock(final Identifier id,
                                   final MutableComponent name,
                                   final ControllerBlockEntityTicker ticker,
                                   final DyeColor color) {
        super(id, ControllerType.CREATIVE, name, ticker, color);
        this.id = id;
    }

    @Override
    public BlockColorMap<
        AbstractControllerBlock<CreativeControllerBlockItem>,
        CreativeControllerBlockItem> getBlockColorMap() {
        return Blocks.INSTANCE.getCreativeController();
    }

    @Override
    public CreativeControllerBlockItem createBlockItem() {
        return new CreativeControllerBlockItem(this, id);
    }
}
