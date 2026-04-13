package com.refinedmods.refinedstorage.common.support;

import java.util.stream.Stream;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

@FunctionalInterface
public interface RecipeProvider {
    <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> getRecipesFor(RecipeType<T> type, I container,
                                                                                       Level level);
}
