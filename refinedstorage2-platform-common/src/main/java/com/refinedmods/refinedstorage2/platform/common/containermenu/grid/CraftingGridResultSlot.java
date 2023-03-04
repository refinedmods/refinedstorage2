package com.refinedmods.refinedstorage2.platform.common.containermenu.grid;

import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.CraftingMatrix;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;

public class CraftingGridResultSlot extends ResultSlot {
    private final CraftingGridSource source;

    public CraftingGridResultSlot(final Player player,
                                  final CraftingGridSource source,
                                  final int x,
                                  final int y) {
        super(player, source.getCraftingMatrix(), source.getCraftingResult(), 0, x, y);
        this.source = source;
    }

    public ItemStack onQuickCraft(final Player player) {
        if (!hasItem() || player.level.isClientSide()) {
            return ItemStack.EMPTY;
        }
        final ItemStack singleResultStack = getItem().copy();
        final int maxCrafted = singleResultStack.getMaxStackSize();
        int crafted = 0;
        try (CraftingGridRefillContext refillContext = source.openSnapshotRefillContext(player)) {
            while (ItemStack.isSameItemSameTags(singleResultStack, getItem()) && crafted < maxCrafted) {
                doTake(player, refillContext);
                crafted += singleResultStack.getCount();
            }
        }
        return singleResultStack.copyWithCount(crafted);
    }

    @Override
    public void onTake(final Player player, final ItemStack stack) {
        if (player.level.isClientSide()) {
            return;
        }
        try (CraftingGridRefillContext refillContext = source.openRefillContext()) {
            doTake(player, refillContext);
        }
    }

    private void doTake(final Player player, final CraftingGridRefillContext refillContext) {
        fireCraftingEvents(player, getItem().copy());
        final NonNullList<ItemStack> remainingItems = source.getRemainingItems(player);
        for (int i = 0; i < remainingItems.size(); ++i) {
            final ItemStack matrixStack = source.getCraftingMatrix().getItem(i);
            final ItemStack remainingItem = remainingItems.get(i);
            if (!remainingItem.isEmpty()) {
                useIngredientWithRemainingItem(player, i, remainingItem);
            } else if (!matrixStack.isEmpty()) {
                useIngredient(player, refillContext, i, matrixStack);
            }
        }
        source.getCraftingMatrix().changed();
    }

    private void useIngredientWithRemainingItem(final Player player,
                                                final int index,
                                                final ItemStack remainingItem) {
        final ItemStack matrixStack = decrementMatrixSlot(index);
        if (matrixStack.isEmpty()) {
            source.getCraftingMatrix().setItem(index, remainingItem);
        } else if (ItemStack.isSameItemSameTags(matrixStack, remainingItem)) {
            remainingItem.grow(matrixStack.getCount());
            source.getCraftingMatrix().setItem(index, remainingItem);
        } else if (!player.getInventory().add(remainingItem)) {
            player.drop(remainingItem, false);
        }
    }

    private void useIngredient(final Player player,
                               final CraftingGridRefillContext refillContext,
                               final int index,
                               final ItemStack matrixStack) {
        if (matrixStack.getCount() > 1 || !refillContext.extract(ItemResource.ofItemStack(matrixStack), player)) {
            decrementMatrixSlot(index);
        }
    }

    private ItemStack decrementMatrixSlot(final int index) {
        final CraftingMatrix matrix = source.getCraftingMatrix();
        matrix.removeItem(index, 1);
        return matrix.getItem(index);
    }

    private void fireCraftingEvents(final Player player, final ItemStack crafted) {
        // reimplementation of checkTakeAchievements
        crafted.onCraftedBy(player.level, player, crafted.getCount());
        Platform.INSTANCE.onItemCrafted(player, crafted, source.getCraftingMatrix());
        if (container instanceof RecipeHolder recipeHolder) {
            recipeHolder.awardUsedRecipes(player);
        }
    }
}
