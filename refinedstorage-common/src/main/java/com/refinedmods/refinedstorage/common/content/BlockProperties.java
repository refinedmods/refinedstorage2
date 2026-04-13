package com.refinedmods.refinedstorage.common.content;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class BlockProperties {
    private BlockProperties() {
    }

    public static BlockBehaviour.Properties stone(final Identifier id) {
        return BlockBehaviour.Properties.of()
            .strength(2.0F, 6.0F)
            .sound(SoundType.STONE)
            .setId(ResourceKey.create(Registries.BLOCK, id));
    }

    public static BlockBehaviour.Properties cable(final Identifier id) {
        return BlockBehaviour.Properties.of()
            .strength(0.35F, 0.35F)
            .noOcclusion()
            .dynamicShape()
            .sound(SoundType.GLASS)
            .setId(ResourceKey.create(Registries.BLOCK, id));
    }
}
