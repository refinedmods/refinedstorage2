package com.refinedmods.refinedstorage.common.support;

import com.refinedmods.refinedstorage.common.content.RecipeSerializers;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Block;

public class RecoloringRecipe extends ShapelessRecipe {
    private final Ingredient ingredient;
    private final Ingredient dye;
    private final Holder<Item> result;

    public RecoloringRecipe(final Ingredient ingredient, final Ingredient dye, final Holder<Item> result) {
        super(
            "",
            CraftingBookCategory.MISC,
            result.value().getDefaultInstance(),
            getIngredients(ingredient, dye)
        );
        this.ingredient = ingredient;
        this.dye = dye;
        this.result = result;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public Ingredient getDye() {
        return dye;
    }

    public Holder<Item> getResult() {
        return result;
    }

    private static NonNullList<Ingredient> getIngredients(final Ingredient ingredient, final Ingredient dye) {
        final NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(ingredient);
        ingredients.add(dye);
        return ingredients;
    }

    @Override
    public ItemStack assemble(final CraftingInput input, final HolderLookup.Provider registries) {
        for (int i = 0; i < input.size(); ++i) {
            final ItemStack stack = input.getItem(i);
            if (ingredient.test(stack)) {
                final ItemStack copied = result.value().getDefaultInstance();
                copied.set(DataComponents.BLOCK_ENTITY_DATA, stack.get(DataComponents.BLOCK_ENTITY_DATA));
                return copied;
            }
        }
        return result.value().getDefaultInstance();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public RecipeSerializer<ShapelessRecipe> getSerializer() {
        return (RecipeSerializer) RecipeSerializers.INSTANCE.getRecoloring();
    }

    @SuppressWarnings("deprecation")
    public static RecoloringRecipe create(final TagKey<Item> ingredient, final DyeColor color, final Block result) {
        return new RecoloringRecipe(
            Ingredient.of(ingredient),
            Ingredient.of(createTag(color)),
            result.asItem().builtInRegistryHolder()
        );
    }

    static TagKey<Item> createTag(final DyeColor color) {
        return TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("c", "dyes/" + color.getSerializedName())
        );
    }
}
