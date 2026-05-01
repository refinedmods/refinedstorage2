package com.refinedmods.refinedstorage.common.content;

import com.refinedmods.refinedstorage.common.support.RecoloringRecipe;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.world.item.crafting.RecipeSerializer;

import static java.util.Objects.requireNonNull;

public final class RecipeSerializers {
    public static final RecipeSerializers INSTANCE = new RecipeSerializers();

    @Nullable
    private Supplier<RecipeSerializer<RecoloringRecipe>> recoloring;

    private RecipeSerializers() {
    }

    public RecipeSerializer<RecoloringRecipe> getRecoloring() {
        return requireNonNull(recoloring).get();
    }

    public void setRecoloring(final Supplier<RecipeSerializer<RecoloringRecipe>> recoloring) {
        this.recoloring = recoloring;
    }
}
