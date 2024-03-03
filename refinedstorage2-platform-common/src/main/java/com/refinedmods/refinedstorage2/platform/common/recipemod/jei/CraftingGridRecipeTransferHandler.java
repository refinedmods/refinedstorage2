package com.refinedmods.refinedstorage2.platform.common.recipemod.jei;

import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.grid.CraftingGridContainerMenu;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

class CraftingGridRecipeTransferHandler implements
    IRecipeTransferHandler<CraftingGridContainerMenu, RecipeHolder<CraftingRecipe>> {
    @Override
    public Class<? extends CraftingGridContainerMenu> getContainerClass() {
        return CraftingGridContainerMenu.class;
    }

    @Override
    public Optional<MenuType<CraftingGridContainerMenu>> getMenuType() {
        return Optional.of(Menus.INSTANCE.getCraftingGrid());
    }

    @Override
    public RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    @Nullable
    public IRecipeTransferError transferRecipe(final CraftingGridContainerMenu containerMenu,
                                               final RecipeHolder<CraftingRecipe> recipe,
                                               final IRecipeSlotsView recipeSlots,
                                               final Player player,
                                               final boolean maxTransfer,
                                               final boolean doTransfer) {
        if (doTransfer) {
            doTransfer(recipeSlots, containerMenu);
            return null;
        }
        final ResourceList available = containerMenu.getAvailableListForRecipeTransfer();
        final List<IRecipeSlotView> missingSlots = findMissingSlots(recipeSlots, available);
        return missingSlots.isEmpty() ? null : new MissingItemRecipeTransferError(missingSlots);
    }

    private void doTransfer(final IRecipeSlotsView recipeSlots, final CraftingGridContainerMenu containerMenu) {
        final List<List<ItemResource>> inputs = getInputs(recipeSlots);
        containerMenu.transferRecipe(inputs);
    }

    private List<IRecipeSlotView> findMissingSlots(final IRecipeSlotsView recipeSlots, final ResourceList available) {
        return recipeSlots.getSlotViews(RecipeIngredientRole.INPUT).stream().filter(slotView -> {
            if (slotView.isEmpty()) {
                return false;
            }
            return !isAvailable(available, slotView);
        }).toList();
    }

    private boolean isAvailable(final ResourceList available, final IRecipeSlotView slotView) {
        final List<ItemStack> possibilities = slotView.getItemStacks().toList();
        for (final ItemStack possibility : possibilities) {
            final ItemResource possibilityResource = ItemResource.ofItemStack(possibility);
            if (available.remove(possibilityResource, 1).isPresent()) {
                return true;
            }
        }
        return false;
    }

    private List<List<ItemResource>> getInputs(final IRecipeSlotsView recipeSlots) {
        return recipeSlots.getSlotViews(RecipeIngredientRole.INPUT).stream().map(slotView -> {
            final List<ItemStack> stacks = slotView.getItemStacks().collect(Collectors.toList());
            prioritizeDisplayedStack(slotView, stacks);
            return stacks.stream().map(ItemResource::ofItemStack).toList();
        }).toList();
    }

    private void prioritizeDisplayedStack(final IRecipeSlotView slotView, final List<ItemStack> stacks) {
        slotView.getDisplayedItemStack().ifPresent(displayed -> {
            final int index = stacks.indexOf(displayed);
            if (index > 0) {
                return;
            }
            stacks.remove(index);
            stacks.add(0, displayed);
        });
    }
}
