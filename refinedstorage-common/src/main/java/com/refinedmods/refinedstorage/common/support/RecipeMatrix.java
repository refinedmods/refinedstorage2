package com.refinedmods.refinedstorage.common.support;

import com.refinedmods.refinedstorage.api.core.NullableType;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.util.ContainerUtil;

import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

public class RecipeMatrix<T extends Recipe<I>, I extends RecipeInput> {
    private final Runnable listener;
    private final Supplier<@NullableType Level> levelSupplier;
    private final RecipeMatrixContainer matrix;
    private final ResultContainer craftingResult = new ResultContainer();
    private final Function<RecipeMatrixContainer, I> inputProvider;
    private final RecipeType<T> recipeType;

    @Nullable
    private RecipeHolder<T> currentRecipe;

    public RecipeMatrix(final Runnable listener,
                        final Supplier<@NullableType Level> levelSupplier,
                        final int width,
                        final int height,
                        final Function<RecipeMatrixContainer, I> inputProvider,
                        final RecipeType<T> recipeType) {
        this.listener = listener;
        this.levelSupplier = levelSupplier;
        this.matrix = new RecipeMatrixContainer(this::matrixChanged, width, height);
        this.inputProvider = inputProvider;
        this.recipeType = recipeType;
    }

    public static RecipeMatrix<CraftingRecipe, CraftingInput> crafting(
        final Runnable listener,
        final Supplier<@NullableType Level> levelSupplier
    ) {
        return new RecipeMatrix<>(
            listener,
            levelSupplier,
            3,
            3,
            RecipeMatrixContainer::asCraftInput,
            RecipeType.CRAFTING
        );
    }

    public static RecipeMatrix<SmithingRecipe, SmithingRecipeInput> smithingTable(
        final Runnable listener,
        final Supplier<@NullableType Level> levelSupplier
    ) {
        return new RecipeMatrix<>(
            listener,
            levelSupplier,
            3,
            1,
            slots -> new SmithingRecipeInput(slots.getItem(0), slots.getItem(1), slots.getItem(2)),
            RecipeType.SMITHING
        );
    }

    private void matrixChanged() {
        final Level level = levelSupplier.get();
        if (level == null) {
            return;
        }
        updateResult(level);
        listener.run();
    }

    public void clear(final Level level) {
        matrix.clearContent();
        updateResult(level);
    }

    public void updateResult(final Level level) {
        if (level.isClientSide()) {
            return;
        }
        final I input = inputProvider.apply(matrix);
        if (currentRecipe == null || !currentRecipe.value().matches(input, level)) {
            currentRecipe = loadRecipe(level);
        }
        if (currentRecipe == null) {
            setResult(null, ItemStack.EMPTY);
        } else {
            setResult(currentRecipe, currentRecipe.value().assemble(input, level.registryAccess()));
        }
    }

    public RecipeMatrixContainer getMatrix() {
        return matrix;
    }

    public ResultContainer getResult() {
        return craftingResult;
    }

    public boolean hasResult() {
        return !craftingResult.getItem(0).isEmpty();
    }

    private void setResult(@Nullable final RecipeHolder<?> recipe, final ItemStack result) {
        craftingResult.setRecipeUsed(recipe);
        craftingResult.setItem(0, result);
    }

    @Nullable
    private RecipeHolder<T> loadRecipe(final Level level) {
        return level
            .getRecipeManager()
            .getRecipeFor(recipeType, inputProvider.apply(matrix), level)
            .orElse(null);
    }

    public NonNullList<ItemStack> getRemainingItems(@Nullable final Level level,
                                                    final Player player,
                                                    final CraftingInput input) {
        if (level == null
            || currentRecipe == null
            || !(currentRecipe.value() instanceof CraftingRecipe craftingRecipe)) {
            return NonNullList.create();
        }
        return Platform.INSTANCE.getRemainingCraftingItems(player, craftingRecipe, input);
    }

    public CompoundTag writeToTag(final HolderLookup.Provider provider) {
        return ContainerUtil.write(matrix, provider);
    }

    public void readFromTag(final CompoundTag tag, final HolderLookup.Provider provider) {
        ContainerUtil.read(tag, matrix, provider);
    }
}
