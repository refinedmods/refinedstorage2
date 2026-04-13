package com.refinedmods.refinedstorage.fabric.support;

import com.refinedmods.refinedstorage.common.support.RecipeProvider;

import java.util.stream.Stream;

import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class SynchronizedRecipesRecipeProvider implements RecipeProvider {
    private final SynchronizedRecipes synchronizedRecipes;

    public SynchronizedRecipesRecipeProvider(final SynchronizedRecipes synchronizedRecipes) {
        this.synchronizedRecipes = synchronizedRecipes;
    }

    @Override
    public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> getRecipesFor(final RecipeType<T> type,
                                                                                              final I container,
                                                                                              final Level level) {
        return synchronizedRecipes.getAllMatches(type, container, level);
    }
}
