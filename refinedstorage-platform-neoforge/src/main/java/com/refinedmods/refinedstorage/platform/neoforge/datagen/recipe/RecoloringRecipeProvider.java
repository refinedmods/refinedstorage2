package com.refinedmods.refinedstorage.platform.neoforge.datagen.recipe;

import com.refinedmods.refinedstorage.platform.common.content.Blocks;
import com.refinedmods.refinedstorage.platform.common.content.Tags;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;

public class RecoloringRecipeProvider extends RecipeProvider {
    public RecoloringRecipeProvider(final PackOutput output,
                                    final CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(final RecipeOutput output) {
        Blocks.INSTANCE.getCable().forEach((color, id, block) ->
            recipe(Tags.CABLES, block.get().asItem(), color)
                .save(output, recipeId(color, "cable")));
        Blocks.INSTANCE.getImporter().forEach((color, id, block) ->
            recipe(Tags.IMPORTERS, block.get().asItem(), color)
                .save(output, recipeId(color, "importer")));
        Blocks.INSTANCE.getExporter().forEach((color, id, block) ->
            recipe(Tags.EXPORTERS, block.get().asItem(), color)
                .save(output, recipeId(color, "exporter")));
        Blocks.INSTANCE.getExternalStorage().forEach((color, id, block) ->
            recipe(Tags.EXTERNAL_STORAGES, block.get().asItem(), color)
                .save(output, recipeId(color, "external_storage")));
        Blocks.INSTANCE.getController().forEach((color, id, block) ->
            recipe(Tags.CONTROLLERS, block.get().asItem(), color)
                .save(output, recipeId(color, "controller")));
        Blocks.INSTANCE.getGrid().forEach((color, id, block) ->
            recipe(Tags.GRIDS, block.get().asItem(), color)
                .save(output, recipeId(color, "grid")));
        Blocks.INSTANCE.getCraftingGrid().forEach((color, id, block) ->
            recipe(Tags.CRAFTING_GRIDS, block.get().asItem(), color)
                .save(output, recipeId(color, "crafting_grid")));
        Blocks.INSTANCE.getDetector().forEach((color, id, block) ->
            recipe(Tags.DETECTORS, block.get().asItem(), color)
                .save(output, recipeId(color, "detector")));
        Blocks.INSTANCE.getConstructor().forEach((color, id, block) ->
            recipe(Tags.CONSTRUCTORS, block.get().asItem(), color)
                .save(output, recipeId(color, "constructor")));
        Blocks.INSTANCE.getDestructor().forEach((color, id, block) ->
            recipe(Tags.DESTRUCTORS, block.get().asItem(), color)
                .save(output, recipeId(color, "destructor")));
        Blocks.INSTANCE.getWirelessTransmitter().forEach((color, id, block) ->
            recipe(Tags.WIRELESS_TRANSMITTERS, block.get().asItem(), color)
                .save(output, recipeId(color, "wireless_transmitter")));
        Blocks.INSTANCE.getNetworkReceiver().forEach((color, id, block) ->
            recipe(Tags.NETWORK_RECEIVERS, block.get().asItem(), color)
                .save(output, recipeId(color, "network_receiver")));
        Blocks.INSTANCE.getNetworkTransmitter().forEach((color, id, block) ->
            recipe(Tags.NETWORK_TRANSMITTERS, block.get().asItem(), color)
                .save(output, recipeId(color, "network_transmitter")));
        Blocks.INSTANCE.getSecurityManager().forEach((color, id, block) ->
            recipe(Tags.SECURITY_MANAGERS, block.get().asItem(), color)
                .save(output, recipeId(color, "security_manager")));
        Blocks.INSTANCE.getRelay().forEach((color, id, block) ->
            recipe(Tags.RELAYS, block.get().asItem(), color)
                .save(output, recipeId(color, "relay")));
        Blocks.INSTANCE.getDiskInterface().forEach((color, id, block) ->
            recipe(Tags.DISK_INTERFACES, block.get().asItem(), color)
                .save(output, recipeId(color, "disk_interface")));
    }

    private ResourceLocation recipeId(final DyeColor color, final String suffix) {
        return createIdentifier("coloring/" + color.getName() + "_" + suffix);
    }

    private ShapelessRecipeBuilder recipe(final TagKey<Item> dyeable, final Item result, final DyeColor color) {
        return ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result)
            .requires(dyeable)
            .requires(getDyeTag(color))
            .unlockedBy("has_" + dyeable.location().getPath(), has(dyeable));
    }

    private static TagKey<Item> getDyeTag(final DyeColor color) {
        return switch (color) {
            case RED -> net.neoforged.neoforge.common.Tags.Items.DYES_RED;
            case WHITE -> net.neoforged.neoforge.common.Tags.Items.DYES_WHITE;
            case ORANGE -> net.neoforged.neoforge.common.Tags.Items.DYES_ORANGE;
            case MAGENTA -> net.neoforged.neoforge.common.Tags.Items.DYES_MAGENTA;
            case LIGHT_BLUE -> net.neoforged.neoforge.common.Tags.Items.DYES_LIGHT_BLUE;
            case YELLOW -> net.neoforged.neoforge.common.Tags.Items.DYES_YELLOW;
            case LIME -> net.neoforged.neoforge.common.Tags.Items.DYES_LIME;
            case PINK -> net.neoforged.neoforge.common.Tags.Items.DYES_PINK;
            case GRAY -> net.neoforged.neoforge.common.Tags.Items.DYES_GRAY;
            case LIGHT_GRAY -> net.neoforged.neoforge.common.Tags.Items.DYES_LIGHT_GRAY;
            case CYAN -> net.neoforged.neoforge.common.Tags.Items.DYES_CYAN;
            case PURPLE -> net.neoforged.neoforge.common.Tags.Items.DYES_PURPLE;
            case BLUE -> net.neoforged.neoforge.common.Tags.Items.DYES_BLUE;
            case BROWN -> net.neoforged.neoforge.common.Tags.Items.DYES_BROWN;
            case GREEN -> net.neoforged.neoforge.common.Tags.Items.DYES_GREEN;
            case BLACK -> net.neoforged.neoforge.common.Tags.Items.DYES_BLACK;
        };
    }
}
