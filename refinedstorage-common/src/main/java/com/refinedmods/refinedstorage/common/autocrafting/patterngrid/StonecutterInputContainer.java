package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import java.util.function.Supplier;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

class StonecutterInputContainer extends SimpleContainer {
    private SelectableRecipe.SingleInputSet<StonecutterRecipe> recipes;
    private final Supplier<@Nullable Level> levelSupplier;
    private int selectedRecipe = -1;

    StonecutterInputContainer(final Supplier<@Nullable Level> levelSupplier) {
        super(1);
        this.levelSupplier = levelSupplier;
        this.recipes = SelectableRecipe.SingleInputSet.empty();
    }

    SelectableRecipe.SingleInputSet<StonecutterRecipe> getRecipes() {
        return recipes;
    }

    int getSelectedRecipe() {
        return selectedRecipe;
    }

    boolean hasSelectedRecipe() {
        return selectedRecipe >= 0;
    }

    void setSelectedRecipe(final int idx) {
        this.selectedRecipe = idx;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        final Level level = levelSupplier.get();
        if (level == null) {
            return;
        }
        this.selectedRecipe = -1;
        this.updateRecipes(level);
    }

    void updateRecipes(final Level level) {
        final ItemStack input = getItem(0);
        if (input.isEmpty()) {
            recipes = SelectableRecipe.SingleInputSet.empty();
            return;
        }
        recipes = level.recipeAccess().stonecutterRecipes().selectByInput(input);
    }
}
