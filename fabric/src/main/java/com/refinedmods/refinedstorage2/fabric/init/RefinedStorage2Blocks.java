package com.refinedmods.refinedstorage2.fabric.init;

import com.refinedmods.refinedstorage2.fabric.block.CableBlock;
import com.refinedmods.refinedstorage2.fabric.block.QuartzEnrichedIronBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Material;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RefinedStorage2Blocks {
    private final FabricBlockSettings rockSettings = FabricBlockSettings.of(Material.STONE).hardness(1.9F).resistance(1.9F).sounds(BlockSoundGroup.STONE);

    private final CableBlock cable = new CableBlock();
    private final QuartzEnrichedIronBlock quartzEnrichedIron = new QuartzEnrichedIronBlock(rockSettings);

    public void register(String namespace) {
        Registry.register(Registry.BLOCK, new Identifier(namespace, "cable"), cable);
        Registry.register(Registry.BLOCK, new Identifier(namespace, "quartz_enriched_iron_block"), quartzEnrichedIron);
    }

    public CableBlock getCable() {
        return cable;
    }

    public QuartzEnrichedIronBlock getQuartzEnrichedIron() {
        return quartzEnrichedIron;
    }
}
