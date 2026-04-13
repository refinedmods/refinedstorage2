package com.refinedmods.refinedstorage.common.controller;

import com.refinedmods.refinedstorage.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.common.content.Blocks;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

public class ControllerBlock extends AbstractControllerBlock<ControllerBlockItem> {
    private final Identifier id;

    public ControllerBlock(final Identifier id,
                           final MutableComponent name,
                           final ControllerBlockEntityTicker ticker,
                           final DyeColor color) {
        super(id, ControllerType.NORMAL, name, ticker, color);
        this.id = id;
    }

    @Override
    public BlockColorMap<AbstractControllerBlock<ControllerBlockItem>, ControllerBlockItem> getBlockColorMap() {
        return Blocks.INSTANCE.getController();
    }

    @Override
    public ControllerBlockItem createBlockItem() {
        return new ControllerBlockItem(id, this);
    }
}
