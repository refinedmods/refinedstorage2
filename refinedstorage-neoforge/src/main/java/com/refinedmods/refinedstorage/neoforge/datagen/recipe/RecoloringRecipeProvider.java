package com.refinedmods.refinedstorage.neoforge.datagen.recipe;

import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.content.Tags;
import com.refinedmods.refinedstorage.common.support.RecoloringRecipe;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public class RecoloringRecipeProvider extends RecipeProvider {
    public RecoloringRecipeProvider(final PackOutput packOutput,
                                    final CompletableFuture<HolderLookup.Provider> registries) {
        super(packOutput, registries);
    }

    @Override
    protected void buildRecipes(final RecipeOutput output, final HolderLookup.Provider registries) {
        Blocks.INSTANCE.getCable().forEach((color, id, block) ->
            output.accept(recipeId(color, "cable"),
                RecoloringRecipe.create(Tags.CABLES, color, block.get()), null));
        Blocks.INSTANCE.getImporter().forEach((color, id, block) ->
            output.accept(recipeId(color, "importer"),
                RecoloringRecipe.create(Tags.IMPORTERS, color, block.get()), null));
        Blocks.INSTANCE.getExporter().forEach((color, id, block) ->
            output.accept(recipeId(color, "exporter"),
                RecoloringRecipe.create(Tags.EXPORTERS, color, block.get()), null));
        Blocks.INSTANCE.getExternalStorage().forEach((color, id, block) ->
            output.accept(recipeId(color, "external_storage"),
                RecoloringRecipe.create(Tags.EXTERNAL_STORAGES, color, block.get()), null));
        Blocks.INSTANCE.getController().forEach((color, id, block) ->
            output.accept(recipeId(color, "controller"),
                RecoloringRecipe.create(Tags.CONTROLLERS, color, block.get()), null));
        Blocks.INSTANCE.getGrid().forEach((color, id, block) ->
            output.accept(recipeId(color, "grid"),
                RecoloringRecipe.create(Tags.GRIDS, color, block.get()), null));
        Blocks.INSTANCE.getCraftingGrid().forEach((color, id, block) ->
            output.accept(recipeId(color, "crafting_grid"),
                RecoloringRecipe.create(Tags.CRAFTING_GRIDS, color, block.get()), null));
        Blocks.INSTANCE.getPatternGrid().forEach((color, id, block) ->
            output.accept(recipeId(color, "pattern_grid"),
                RecoloringRecipe.create(Tags.PATTERN_GRIDS, color, block.get()), null));
        Blocks.INSTANCE.getDetector().forEach((color, id, block) ->
            output.accept(recipeId(color, "detector"),
                RecoloringRecipe.create(Tags.DETECTORS, color, block.get()), null));
        Blocks.INSTANCE.getConstructor().forEach((color, id, block) ->
            output.accept(recipeId(color, "constructor"),
                RecoloringRecipe.create(Tags.CONSTRUCTORS, color, block.get()), null));
        Blocks.INSTANCE.getDestructor().forEach((color, id, block) ->
            output.accept(recipeId(color, "destructor"),
                RecoloringRecipe.create(Tags.DESTRUCTORS, color, block.get()), null));
        Blocks.INSTANCE.getWirelessTransmitter().forEach((color, id, block) ->
            output.accept(recipeId(color, "wireless_transmitter"),
                RecoloringRecipe.create(Tags.WIRELESS_TRANSMITTERS, color, block.get()), null));
        Blocks.INSTANCE.getNetworkReceiver().forEach((color, id, block) ->
            output.accept(recipeId(color, "network_receiver"),
                RecoloringRecipe.create(Tags.NETWORK_RECEIVERS, color, block.get()), null));
        Blocks.INSTANCE.getNetworkTransmitter().forEach((color, id, block) ->
            output.accept(recipeId(color, "network_transmitter"),
                RecoloringRecipe.create(Tags.NETWORK_TRANSMITTERS, color, block.get()), null));
        Blocks.INSTANCE.getSecurityManager().forEach((color, id, block) ->
            output.accept(recipeId(color, "security_manager"),
                RecoloringRecipe.create(Tags.SECURITY_MANAGERS, color, block.get()), null));
        Blocks.INSTANCE.getRelay().forEach((color, id, block) ->
            output.accept(recipeId(color, "relay"),
                RecoloringRecipe.create(Tags.RELAYS, color, block.get()), null));
        Blocks.INSTANCE.getDiskInterface().forEach((color, id, block) ->
            output.accept(recipeId(color, "disk_interface"),
                RecoloringRecipe.create(Tags.DISK_INTERFACES, color, block.get()), null));
        Blocks.INSTANCE.getAutocrafter().forEach((color, id, block) ->
            output.accept(recipeId(color, "autocrafter"),
                RecoloringRecipe.create(Tags.AUTOCRAFTERS, color, block.get()), null));
        Blocks.INSTANCE.getAutocrafterManager().forEach((color, id, block) ->
            output.accept(recipeId(color, "autocrafter_manager"),
                RecoloringRecipe.create(Tags.AUTOCRAFTER_MANAGERS, color, block.get()), null));
        Blocks.INSTANCE.getAutocraftingMonitor().forEach((color, id, block) ->
            output.accept(recipeId(color, "autocrafting_monitor"),
                RecoloringRecipe.create(Tags.AUTOCRAFTING_MONITORS, color, block.get()), null));
    }

    private ResourceLocation recipeId(final DyeColor color, final String suffix) {
        return createIdentifier("coloring/" + color.getName() + "_" + suffix);
    }
}
