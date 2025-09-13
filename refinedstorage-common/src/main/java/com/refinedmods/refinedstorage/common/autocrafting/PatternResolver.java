package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternLayout;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.content.DataComponents;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

public class PatternResolver {
    PatternResolver() {
    }

    Optional<ResolvedCraftingPattern> getCraftingPattern(final ItemStack stack,
                                                         final Level level,
                                                         final PatternState patternState) {
        final CraftingPatternState craftingState = stack.get(DataComponents.INSTANCE.getCraftingPatternState());
        if (craftingState == null) {
            return Optional.empty();
        }
        return getCraftingPattern(level, patternState, craftingState);
    }

    private Optional<ResolvedCraftingPattern> getCraftingPattern(final Level level,
                                                                 final PatternState patternState,
                                                                 final CraftingPatternState state) {
        final RecipeMatrixContainer craftingMatrix = getFilledCraftingMatrix(state);
        final CraftingInput.Positioned positionedCraftingInput = craftingMatrix.asPositionedCraftInput();
        final CraftingInput craftingInput = positionedCraftingInput.input();
        return level.getRecipeManager()
            .getRecipeFor(RecipeType.CRAFTING, craftingInput, level)
            .map(RecipeHolder::value)
            .map(recipe -> toCraftingPattern(level, recipe, craftingInput, state, patternState));
    }

    private RecipeMatrixContainer getFilledCraftingMatrix(final CraftingPatternState state) {
        final CraftingInput.Positioned positionedInput = state.input();
        final CraftingInput input = positionedInput.input();
        final RecipeMatrixContainer craftingMatrix = new RecipeMatrixContainer(null, input.width(), input.height());
        for (int i = 0; i < input.size(); ++i) {
            craftingMatrix.setItem(i, input.getItem(i));
        }
        return craftingMatrix;
    }

    private ResolvedCraftingPattern toCraftingPattern(final Level level,
                                                      final CraftingRecipe recipe,
                                                      final CraftingInput craftingInput,
                                                      final CraftingPatternState state,
                                                      final PatternState patternState) {
        final List<List<ResourceKey>> inputs = getInputs(recipe, state);
        final ResourceAmount output = getOutput(level, recipe, craftingInput);
        final List<ResourceAmount> byproducts = getByproducts(recipe, craftingInput);
        return new ResolvedCraftingPattern(patternState.id(), inputs, output, byproducts);
    }

    private List<List<ResourceKey>> getInputs(final CraftingRecipe recipe, final CraftingPatternState state) {
        final List<List<ResourceKey>> inputs = new ArrayList<>();
        for (int i = 0; i < state.input().input().size(); ++i) {
            final ItemStack input = state.input().input().getItem(i);
            if (input.isEmpty()) {
                inputs.add(Collections.emptyList());
            } else if (state.fuzzyMode()) {
                inputs.add(getFuzzyInput(recipe, state, i, input));
            } else {
                inputs.add(List.of(ItemResource.ofItemStack(input)));
            }
        }
        return inputs;
    }

    private List<ResourceKey> getFuzzyInput(final CraftingRecipe recipe, final CraftingPatternState state,
                                            final int index, final ItemStack input) {
        final int width = state.input().input().width();
        final boolean mirror = isMirror(recipe.getIngredients(), state.input().input(), width);
        final int col = index % width;
        final int row = index / width;
        final int ingredientIndex = mirror
            ? (width - 1 - col) + row * width
            : index;
        if (ingredientIndex >= 0 && ingredientIndex < recipe.getIngredients().size()) {
            final ItemStack[] ingredients = recipe.getIngredients().get(ingredientIndex).getItems();
            return Arrays.stream(ingredients)
                .map(item -> (ResourceKey) ItemResource.ofItemStack(item))
                .toList();
        }
        return List.of(ItemResource.ofItemStack(input));
    }

    private boolean isMirror(final List<net.minecraft.world.item.crafting.Ingredient> ingredients,
                             final CraftingInput craftingInput,
                             final int width) {
        for (int i = 0; i < craftingInput.size(); i++) {
            final ItemStack input = craftingInput.getItem(i);
            if (input.isEmpty()) {
                continue;
            }
            final int row = i / width;
            final int col = i % width;
            final int idx = row * width + col;
            if (idx < ingredients.size() && ingredients.get(idx).test(input)) {
                return false;
            }
            final int mirroredIdx = row * width + (width - 1 - col);
            if (mirroredIdx < ingredients.size() && ingredients.get(mirroredIdx).test(input)) {
                return true;
            }
        }
        return false;
    }

    private ResourceAmount getOutput(final Level level,
                                     final CraftingRecipe recipe,
                                     final CraftingInput craftingInput) {
        final ItemStack outputStack = recipe.assemble(craftingInput, level.registryAccess());
        return new ResourceAmount(ItemResource.ofItemStack(outputStack), outputStack.getCount());
    }

    private List<ResourceAmount> getByproducts(final CraftingRecipe recipe, final CraftingInput craftingInput) {
        return recipe.getRemainingItems(craftingInput)
            .stream()
            .filter(byproduct -> !byproduct.isEmpty())
            .map(byproduct -> new ResourceAmount(ItemResource.ofItemStack(byproduct), byproduct.getCount()))
            .toList();
    }

    Optional<ResolvedProcessingPattern> getProcessingPattern(final PatternState patternState, final ItemStack stack) {
        final ProcessingPatternState state = stack.get(DataComponents.INSTANCE.getProcessingPatternState());
        if (state == null || state.getIngredients().isEmpty() || state.getFlatOutputs().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(
            new ResolvedProcessingPattern(patternState.id(), state.getIngredients(), state.getFlatOutputs())
        );
    }

    Optional<ResolvedStonecutterPattern> getStonecutterPattern(final ItemStack stack,
                                                               final Level level,
                                                               final PatternState patternState) {
        final StonecutterPatternState state = stack.get(DataComponents.INSTANCE.getStonecutterPatternState());
        if (state == null) {
            return Optional.empty();
        }
        return getStonecutterPattern(level, patternState, state);
    }

    private Optional<ResolvedStonecutterPattern> getStonecutterPattern(final Level level,
                                                                       final PatternState patternState,
                                                                       final StonecutterPatternState state) {
        final SingleRecipeInput input = new SingleRecipeInput(state.input().toItemStack());
        final ItemStack selectedOutput = state.selectedOutput().toItemStack();
        final var recipes = level.getRecipeManager().getRecipesFor(RecipeType.STONECUTTING, input, level);
        for (final var recipe : recipes) {
            final ItemStack output = recipe.value().assemble(input, level.registryAccess());
            if (ItemStack.isSameItemSameComponents(output, selectedOutput)) {
                return Optional.of(new ResolvedStonecutterPattern(
                    patternState.id(),
                    state.input(),
                    ItemResource.ofItemStack(output)
                ));
            }
        }
        return Optional.empty();
    }

    Optional<ResolvedSmithingTablePattern> getSmithingTablePattern(final PatternState patternState,
                                                                   final ItemStack stack,
                                                                   final Level level) {
        final SmithingTablePatternState state = stack.get(DataComponents.INSTANCE.getSmithingTablePatternState());
        if (state == null) {
            return Optional.empty();
        }
        return getSmithingTablePattern(level, patternState, state);
    }

    private Optional<ResolvedSmithingTablePattern> getSmithingTablePattern(final Level level,
                                                                           final PatternState patternState,
                                                                           final SmithingTablePatternState state) {
        final SmithingRecipeInput input = new SmithingRecipeInput(
            state.template().toItemStack(),
            state.base().toItemStack(),
            state.addition().toItemStack()
        );
        return level.getRecipeManager()
            .getRecipeFor(RecipeType.SMITHING, input, level)
            .map(recipe -> new ResolvedSmithingTablePattern(
                patternState.id(),
                state.template(),
                state.base(),
                state.addition(),
                ItemResource.ofItemStack(recipe.value().assemble(input, level.registryAccess())))
            );
    }

    public record ResolvedCraftingPattern(List<List<ResourceKey>> inputs,
                                          ResourceAmount output,
                                          Pattern pattern) {
        ResolvedCraftingPattern(final UUID id,
                                final List<List<ResourceKey>> inputs,
                                final ResourceAmount output,
                                final List<ResourceAmount> byproducts) {
            this(inputs, output, new Pattern(id, PatternLayout.internal(
                inputs.stream()
                    .filter(i -> !i.isEmpty())
                    .map(i -> new Ingredient(1, i))
                    .toList(),
                List.of(output),
                byproducts
            )));
        }
    }

    public record ResolvedProcessingPattern(Pattern pattern) {
        ResolvedProcessingPattern(final UUID id,
                                  final List<Ingredient> ingredients,
                                  final List<ResourceAmount> outputs) {
            this(new Pattern(id, PatternLayout.external(ingredients, outputs)));
        }
    }

    public record ResolvedStonecutterPattern(ItemResource input,
                                             ItemResource output,
                                             Pattern pattern) {
        ResolvedStonecutterPattern(final UUID id, final ItemResource input, final ItemResource output) {
            this(input, output, new Pattern(id, PatternLayout.internal(
                List.of(new Ingredient(1, List.of(input))),
                List.of(new ResourceAmount(output, 1)),
                List.of()
            )));
        }
    }

    public record ResolvedSmithingTablePattern(ItemResource template,
                                               ItemResource base,
                                               ItemResource addition,
                                               ItemResource output,
                                               Pattern pattern) {
        ResolvedSmithingTablePattern(final UUID id,
                                     final ItemResource template,
                                     final ItemResource base,
                                     final ItemResource addition,
                                     final ItemResource output) {
            this(template, base, addition, output, new Pattern(id, PatternLayout.internal(
                List.of(single(template), single(base), single(addition)),
                List.of(new ResourceAmount(output, 1)),
                List.of()
            )));
        }

        private static Ingredient single(final ResourceKey input) {
            return new Ingredient(1, List.of(input));
        }
    }
}
