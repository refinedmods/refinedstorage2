package com.refinedmods.refinedstorage.neoforge.datagen.tag;

import com.refinedmods.refinedstorage.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;
import com.refinedmods.refinedstorage.common.support.BlockItemProvider;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.MOD_ID;

public class BlockTagsProvider extends IntrinsicHolderTagsProvider<Block> {
    public static final TagKey<Block> MINEABLE = TagKey.create(Registries.BLOCK,
        Identifier.withDefaultNamespace("mineable/pickaxe"));

    @SuppressWarnings("deprecation")
    public BlockTagsProvider(final PackOutput packOutput, final CompletableFuture<HolderLookup.Provider> registries) {
        super(packOutput, Registries.BLOCK, registries, block -> block.builtInRegistryHolder().key(), MOD_ID);
    }

    @Override
    protected void addTags(final HolderLookup.Provider provider) {
        markAsMineable(Blocks.INSTANCE.getCable());
        markAsMineable(Blocks.INSTANCE.getDiskDrive());
        markAsMineable(Blocks.INSTANCE.getMachineCasing());
        markAsMineable(Blocks.INSTANCE.getGrid());
        markAsMineable(Blocks.INSTANCE.getCraftingGrid());
        markAsMineable(Blocks.INSTANCE.getPatternGrid());
        markAsMineable(Blocks.INSTANCE.getController());
        markAsMineable(Blocks.INSTANCE.getCreativeController());
        for (final ItemStorageVariant variant : ItemStorageVariant.values()) {
            markAsMineable(Blocks.INSTANCE.getItemStorageBlock(variant));
        }
        for (final FluidStorageVariant variant : FluidStorageVariant.values()) {
            markAsMineable(Blocks.INSTANCE.getFluidStorageBlock(variant));
        }
        markAsMineable(Blocks.INSTANCE.getImporter());
        markAsMineable(Blocks.INSTANCE.getExporter());
        markAsMineable(Blocks.INSTANCE.getInterface());
        markAsMineable(Blocks.INSTANCE.getExternalStorage());
        markAsMineable(Blocks.INSTANCE.getDetector());
        markAsMineable(Blocks.INSTANCE.getDestructor());
        markAsMineable(Blocks.INSTANCE.getConstructor());
        markAsMineable(Blocks.INSTANCE.getWirelessTransmitter());
        markAsMineable(Blocks.INSTANCE.getStorageMonitor());
        markAsMineable(Blocks.INSTANCE.getNetworkReceiver());
        markAsMineable(Blocks.INSTANCE.getNetworkTransmitter());
        markAsMineable(Blocks.INSTANCE.getPortableGrid());
        markAsMineable(Blocks.INSTANCE.getCreativePortableGrid());
        markAsMineable(Blocks.INSTANCE.getSecurityManager());
        markAsMineable(Blocks.INSTANCE.getRelay());
        markAsMineable(Blocks.INSTANCE.getDiskInterface());
        markAsMineable(Blocks.INSTANCE.getAutocrafter());
        markAsMineable(Blocks.INSTANCE.getAutocrafterManager());
        markAsMineable(Blocks.INSTANCE.getAutocraftingMonitor());
    }

    private <T extends Block & BlockItemProvider<I>, I extends BlockItem> void markAsMineable(
        final BlockColorMap<T, I> map
    ) {
        tag(MINEABLE).addAll(map.values().stream().map(b -> (Block) b).toList());
    }

    private void markAsMineable(final Block block) {
        tag(MINEABLE).add(block);
    }
}
