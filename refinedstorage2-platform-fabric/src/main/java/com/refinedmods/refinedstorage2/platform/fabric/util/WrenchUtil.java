package com.refinedmods.refinedstorage2.platform.fabric.util;

import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;

import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.sound.SoundCategory;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class WrenchUtil {
    private static final Tag<Item> WRENCHES = TagFactory.ITEM.create(new Identifier("fabric:wrenches"));
    private static final Tag<Block> WRENCHABLES = TagFactory.BLOCK.create(new Identifier("fabric:wrenchables"));

    private WrenchUtil() {
    }

    public static boolean isWrench(Item item) {
        return WRENCHES.contains(item);
    }

    public static boolean isWrenchable(BlockState blockState) {
        return blockState.isIn(WRENCHABLES);
    }

    public static void playWrenchSound(World world, BlockPos pos) {
        world.playSound(null, pos, Rs2Mod.getWrenchSoundEvent(), SoundCategory.BLOCKS, 1.0F, 1.0F);
    }
}
