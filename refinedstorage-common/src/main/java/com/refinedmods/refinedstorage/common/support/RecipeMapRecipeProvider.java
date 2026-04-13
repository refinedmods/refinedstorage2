package com.refinedmods.refinedstorage.common.support;

import java.util.stream.Stream;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class RecipeMapRecipeProvider implements RecipeProvider {
    private final RecipeMap recipeMap;

    public RecipeMapRecipeProvider(final RecipeMap recipeMap) {
        this.recipeMap = recipeMap;
    }

    @Override
    public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> getRecipesFor(final RecipeType<T> type,
                                                                                              final I container,
                                                                                              final Level level) {
        return recipeMap.getRecipesFor(type, container, level);
    }
}
