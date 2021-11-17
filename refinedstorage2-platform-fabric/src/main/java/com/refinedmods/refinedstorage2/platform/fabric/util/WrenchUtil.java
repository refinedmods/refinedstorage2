package com.refinedmods.refinedstorage2.platform.fabric.util;

import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;

import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class WrenchUtil {
    private static final Tag<Item> WRENCHES = TagFactory.ITEM.create(new ResourceLocation("fabric:wrenches"));
    private static final Tag<Block> WRENCHABLES = TagFactory.BLOCK.create(new ResourceLocation("fabric:wrenchables"));

    private WrenchUtil() {
    }

    public static boolean isWrench(Item item) {
        return WRENCHES.contains(item);
    }

    public static boolean isWrenchable(BlockState blockState) {
        return blockState.is(WRENCHABLES);
    }

    public static void playWrenchSound(Level world, BlockPos pos) {
        world.playSound(null, pos, Rs2Mod.getWrenchSoundEvent(), SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
