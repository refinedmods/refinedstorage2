package com.refinedmods.refinedstorage.neoforge.datagen.loot;

import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.support.energy.EnergyLootItemFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;

public class BlockDropProvider extends BlockLootSubProvider {
    public BlockDropProvider(final HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        Blocks.INSTANCE.getCable().forEach((color, id, block) -> drop(block.get()));
        Blocks.INSTANCE.getGrid().forEach((color, id, block) -> drop(block.get()));
        Blocks.INSTANCE.getCraftingGrid().forEach((color, id, block) -> drop(block.get()));
        Blocks.INSTANCE.getPatternGrid().forEach((color, id, block) -> drop(block.get()));
        Blocks.INSTANCE.getController().forEach((color, id, block) -> add(
            block.get(),
            createSingleItemTable(block.get())
                .apply(EnergyLootItemFunction::new)
                .apply(copyName())
        ));
        Blocks.INSTANCE.getCreativeController().forEach((color, id, block) -> drop(block.get()));
        Blocks.INSTANCE.getDetector().forEach((color, id, block) -> drop(block.get()));
        Blocks.INSTANCE.getConstructor().forEach((color, id, block) -> drop(block.get()));
        Blocks.INSTANCE.getDestructor().forEach((color, id, block) -> drop(block.get()));
        Blocks.INSTANCE.getImporter().forEach((color, id, block) -> drop(block.get()));
        Blocks.INSTANCE.getExporter().forEach((color, id, block) -> drop(block.get()));
        Blocks.INSTANCE.getWirelessTransmitter().forEach((color, id, block) -> drop(block.get()));
        Blocks.INSTANCE.getNetworkReceiver().forEach((color, id, block) -> drop(block.get()));
        Blocks.INSTANCE.getNetworkTransmitter().forEach((color, id, block) -> drop(block.get()));
        Blocks.INSTANCE.getSecurityManager().forEach((color, id, block) -> drop(block.get()));
        Blocks.INSTANCE.getRelay().forEach((color, id, block) -> drop(block.get()));
        Blocks.INSTANCE.getDiskInterface().forEach((color, id, block) -> drop(block.get()));
        Blocks.INSTANCE.getAutocrafter().forEach((color, id, block) -> drop(block.get()));
        Blocks.INSTANCE.getAutocrafterManager().forEach((color, id, block) -> drop(block.get()));
        Blocks.INSTANCE.getAutocraftingMonitor().forEach((color, id, block) -> drop(block.get()));
    }

    private void drop(final Block block) {
        add(block, createSingleItemTable(block)
            .apply(copyName()));
    }

    private static CopyComponentsFunction.Builder copyName() {
        return CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
            .include(DataComponents.CUSTOM_NAME);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        final List<Block> blocks = new ArrayList<>();
        blocks.addAll(Blocks.INSTANCE.getCable().values());
        blocks.addAll(Blocks.INSTANCE.getGrid().values());
        blocks.addAll(Blocks.INSTANCE.getCraftingGrid().values());
        blocks.addAll(Blocks.INSTANCE.getPatternGrid().values());
        blocks.addAll(Blocks.INSTANCE.getController().values());
        blocks.addAll(Blocks.INSTANCE.getCreativeController().values());
        blocks.addAll(Blocks.INSTANCE.getDetector().values());
        blocks.addAll(Blocks.INSTANCE.getConstructor().values());
        blocks.addAll(Blocks.INSTANCE.getDestructor().values());
        blocks.addAll(Blocks.INSTANCE.getImporter().values());
        blocks.addAll(Blocks.INSTANCE.getExporter().values());
        blocks.addAll(Blocks.INSTANCE.getWirelessTransmitter().values());
        blocks.addAll(Blocks.INSTANCE.getNetworkReceiver().values());
        blocks.addAll(Blocks.INSTANCE.getNetworkTransmitter().values());
        blocks.addAll(Blocks.INSTANCE.getSecurityManager().values());
        blocks.addAll(Blocks.INSTANCE.getRelay().values());
        blocks.addAll(Blocks.INSTANCE.getDiskInterface().values());
        blocks.addAll(Blocks.INSTANCE.getAutocrafter().values());
        blocks.addAll(Blocks.INSTANCE.getAutocrafterManager().values());
        blocks.addAll(Blocks.INSTANCE.getAutocraftingMonitor().values());
        return blocks;
    }
}
