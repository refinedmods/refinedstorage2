package com.refinedmods.refinedstorage2.platform.fabric.internal;

import com.refinedmods.refinedstorage2.platform.abstractions.WrenchHelper;

import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class WrenchHelperImpl implements WrenchHelper {
    private final Tag<Item> wrenches = TagFactory.ITEM.create(new ResourceLocation("fabric:wrenches"));
    private final Tag<Block> wrenchables = TagFactory.BLOCK.create(new ResourceLocation("fabric:wrenchables"));

    @Override
    public boolean isWrench(Item item) {
        return wrenches.contains(item);
    }

    @Override
    public boolean isWrenchable(BlockState blockState) {
        return blockState.is(wrenchables);
    }
}
