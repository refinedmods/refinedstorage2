package com.refinedmods.refinedstorage2.platform.common.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

public final class BlockConstants {
    public static final BlockBehaviour.Properties PROPERTIES = BlockBehaviour.Properties
        .of(Material.STONE)
        .strength(0.5F, 6.0F)
        .sound(SoundType.STONE);

    public static final BlockBehaviour.Properties CABLE_PROPERTIES = BlockBehaviour.Properties
        .of(Material.GLASS)
        .strength(0.35F, 0.35F)
        .sound(SoundType.GLASS);

    private BlockConstants() {
    }
}
