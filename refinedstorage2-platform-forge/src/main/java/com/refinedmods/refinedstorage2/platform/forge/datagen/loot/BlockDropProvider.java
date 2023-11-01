package com.refinedmods.refinedstorage2.platform.forge.datagen.loot;

import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.support.energy.EnergyLootItemFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public class BlockDropProvider extends BlockLootSubProvider {
    public BlockDropProvider() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        Blocks.INSTANCE.getCable().forEach((color, id, block) -> dropSelf(block.get()));
        Blocks.INSTANCE.getGrid().forEach((color, id, block) -> dropSelf(block.get()));
        Blocks.INSTANCE.getCraftingGrid().forEach((color, id, block) -> dropSelf(block.get()));
        Blocks.INSTANCE.getController().forEach((color, id, block) -> add(
            block.get(),
            createSingleItemTable(block.get()).apply(EnergyLootItemFunction::new)
        ));
        Blocks.INSTANCE.getCreativeController().forEach((color, id, block) -> dropSelf(block.get()));
        Blocks.INSTANCE.getDetector().forEach((color, id, block) -> dropSelf(block.get()));
        Blocks.INSTANCE.getConstructor().forEach((color, id, block) -> dropSelf(block.get()));
        Blocks.INSTANCE.getDestructor().forEach((color, id, block) -> dropSelf(block.get()));
        Blocks.INSTANCE.getWirelessTransmitter().forEach((color, id, block) -> dropSelf(block.get()));
        Blocks.INSTANCE.getNetworkReceiver().forEach((color, id, block) -> dropSelf(block.get()));
        Blocks.INSTANCE.getNetworkTransmitter().forEach((color, id, block) -> dropSelf(block.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        final List<Block> blocks = new ArrayList<>();
        blocks.addAll(Blocks.INSTANCE.getCable().values());
        blocks.addAll(Blocks.INSTANCE.getGrid().values());
        blocks.addAll(Blocks.INSTANCE.getCraftingGrid().values());
        blocks.addAll(Blocks.INSTANCE.getController().values());
        blocks.addAll(Blocks.INSTANCE.getCreativeController().values());
        blocks.addAll(Blocks.INSTANCE.getDetector().values());
        blocks.addAll(Blocks.INSTANCE.getConstructor().values());
        blocks.addAll(Blocks.INSTANCE.getDestructor().values());
        blocks.addAll(Blocks.INSTANCE.getWirelessTransmitter().values());
        blocks.addAll(Blocks.INSTANCE.getNetworkReceiver().values());
        blocks.addAll(Blocks.INSTANCE.getNetworkTransmitter().values());
        return blocks;
    }
}
