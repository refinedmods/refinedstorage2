package com.refinedmods.refinedstorage2.fabric.init;

import com.refinedmods.refinedstorage2.fabric.block.CableBlock;
import com.refinedmods.refinedstorage2.fabric.block.QuartzEnrichedIronBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Material;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RefinedStorage2Blocks {
    private static final FabricBlockSettings STONE_SETTINGS = FabricBlockSettings
        .of(Material.STONE)
        .hardness(1.9F)
        .resistance(1.9F)
        .sounds(BlockSoundGroup.STONE);

    private CableBlock cable;
    private QuartzEnrichedIronBlock quartzEnrichedIron;

    public void register(String namespace) {
        cable = Registry.register(Registry.BLOCK, new Identifier(namespace, "cable"), new CableBlock());
        quartzEnrichedIron = Registry.register(Registry.BLOCK, new Identifier(namespace, "quartz_enriched_iron_block"), new QuartzEnrichedIronBlock(STONE_SETTINGS));
    }

    public CableBlock getCable() {
        return cable;
    }

    public QuartzEnrichedIronBlock getQuartzEnrichedIron() {
        return quartzEnrichedIron;
    }
}
