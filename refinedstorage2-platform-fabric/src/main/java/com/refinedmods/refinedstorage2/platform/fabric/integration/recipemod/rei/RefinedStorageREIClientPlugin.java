package com.refinedmods.refinedstorage2.platform.fabric.integration.recipemod.rei;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.common.block.ColorableBlock;
import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.ContentIds;
import com.refinedmods.refinedstorage2.platform.common.content.Tags;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

@Environment(EnvType.CLIENT)
public class RefinedStorageREIClientPlugin implements REIClientPlugin {
    @Override
    public void registerScreens(final ScreenRegistry registry) {
        final IngredientConverter converter = PlatformApi.INSTANCE.getIngredientConverter();
        registry.registerFocusedStack(new GridFocusedStackProvider(converter));
        registry.registerFocusedStack(new FilteredResourceFocusedStackProvider(converter));
    }

    @Override
    public void registerTransferHandlers(final TransferHandlerRegistry registry) {
        registry.register(new CraftingGridTransferHandler());
    }

    public static void registerIngredientConverters() {
        PlatformApi.INSTANCE.registerIngredientConverter(new GridResourceIngredientConverter());
        PlatformApi.INSTANCE.registerIngredientConverter(new FilteredResourceIngredientConverter());
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void registerCollapsibleEntries(final CollapsibleEntryRegistry registry) {
        groupItems(registry, Blocks.INSTANCE.getCable(), ContentIds.CABLE, Tags.CABLES);
        groupItems(registry, Blocks.INSTANCE.getGrid(), ContentIds.GRID, Tags.GRIDS);
        groupItems(registry, Blocks.INSTANCE.getCraftingGrid(), ContentIds.CRAFTING_GRID, Tags.CRAFTING_GRIDS);
        groupItems(registry, Blocks.INSTANCE.getImporter(), ContentIds.IMPORTER, Tags.IMPORTERS);
        groupItems(registry, Blocks.INSTANCE.getExporter(), ContentIds.EXPORTER, Tags.EXPORTERS);
        groupItems(registry, Blocks.INSTANCE.getExternalStorage(), ContentIds.EXTERNAL_STORAGE, Tags.EXTERNAL_STORAGES);
        groupItems(registry, Blocks.INSTANCE.getController(), ContentIds.CONTROLLER, Tags.CONTROLLERS);
        groupItems(registry, Blocks.INSTANCE.getCreativeController(),
            ContentIds.CREATIVE_CONTROLLER, Tags.CREATIVE_CONTROLLERS);
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void groupItems(
        final CollapsibleEntryRegistry registry,
        final BlockColorMap<? extends ColorableBlock<? extends Block>> blocks,
        final ResourceLocation itemIdentifier,
        final TagKey<Item> tag
    ) {
        registry.group(
            itemIdentifier,
            blocks.getDefault().getName(),
            EntryIngredients.ofItemTag(tag)
        );
    }
}
