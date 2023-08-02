package com.refinedmods.refinedstorage2.platform.forge.datagen.recipe;

import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.Tags;

import java.util.function.Consumer;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class RecoloringRecipeProvider extends RecipeProvider {
    public RecoloringRecipeProvider(final PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(final Consumer<FinishedRecipe> provider) {
        Blocks.INSTANCE.getCable().forEach((color, id, block) ->
            recolorItems(Tags.CABLES, block.get().asItem(), color)
                .save(provider, createIdentifier("coloring/" + color.getName() + "_cable")));
        Blocks.INSTANCE.getImporter().forEach((color, id, block) ->
            recolorItems(Tags.IMPORTERS, block.get().asItem(), color)
                .save(provider, createIdentifier("coloring/" + color.getName() + "_importer")));
        Blocks.INSTANCE.getExporter().forEach((color, id, block) ->
            recolorItems(Tags.EXPORTERS, block.get().asItem(), color)
                .save(provider, createIdentifier("coloring/" + color.getName() + "_exporter")));
        Blocks.INSTANCE.getExternalStorage().forEach((color, id, block) ->
            recolorItems(Tags.EXTERNAL_STORAGES, block.get().asItem(), color)
                .save(provider, createIdentifier("coloring/" + color.getName() + "_external_storage")));
        Blocks.INSTANCE.getController().forEach((color, id, block) ->
            recolorItems(Tags.CONTROLLERS, block.get().asItem(), color)
                .save(provider, createIdentifier("coloring/" + color.getName() + "_controller")));
        Blocks.INSTANCE.getGrid().forEach((color, id, block) ->
            recolorItems(Tags.GRIDS, block.get().asItem(), color)
                .save(provider, createIdentifier("coloring/" + color.getName() + "_grid")));
        Blocks.INSTANCE.getCraftingGrid().forEach((color, id, block) ->
            recolorItems(Tags.CRAFTING_GRIDS, block.get().asItem(), color)
                .save(provider, createIdentifier("coloring/" + color.getName() + "_crafting_grid")));
        Blocks.INSTANCE.getDetector().forEach((color, id, block) ->
            recolorItems(Tags.DETECTORS, block.get().asItem(), color)
                .save(provider, createIdentifier("coloring/" + color.getName() + "_detector")));
        Blocks.INSTANCE.getConstructor().forEach((color, id, block) ->
            recolorItems(Tags.CONSTRUCTORS, block.get().asItem(), color)
                .save(provider, createIdentifier("coloring/" + color.getName() + "_constructor")));
        Blocks.INSTANCE.getDestructor().forEach((color, id, block) ->
            recolorItems(Tags.DESTRUCTORS, block.get().asItem(), color)
                .save(provider, createIdentifier("coloring/" + color.getName() + "_destructor")));
    }

    private ShapelessRecipeBuilder recolorItems(final TagKey<Item> dyeable, final Item result, final DyeColor color) {
        return ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result)
            .requires(dyeable)
            .requires(getDyeItem(color))
            .unlockedBy("has_" + dyeable.location().getPath(), has(dyeable));
    }

    private static Item getDyeItem(final DyeColor color) {
        return switch (color) {
            case RED -> Items.RED_DYE;
            case WHITE -> Items.WHITE_DYE;
            case ORANGE -> Items.ORANGE_DYE;
            case MAGENTA -> Items.MAGENTA_DYE;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_DYE;
            case YELLOW -> Items.YELLOW_DYE;
            case LIME -> Items.LIME_DYE;
            case PINK -> Items.PINK_DYE;
            case GRAY -> Items.GRAY_DYE;
            case LIGHT_GRAY -> Items.LIGHT_GRAY_DYE;
            case CYAN -> Items.CYAN_DYE;
            case PURPLE -> Items.PURPLE_DYE;
            case BLUE -> Items.BLUE_DYE;
            case BROWN -> Items.BROWN_DYE;
            case GREEN -> Items.GREEN_DYE;
            case BLACK -> Items.BLACK_DYE;
        };
    }
}
