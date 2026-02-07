package com.refinedmods.refinedstorage.common.support;

import com.refinedmods.refinedstorage.common.content.BlockProperties;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public final class SimpleStoneBlock extends Block {
    public SimpleStoneBlock(final Identifier id) {
        super(BlockProperties.stone(id));
    }
}
