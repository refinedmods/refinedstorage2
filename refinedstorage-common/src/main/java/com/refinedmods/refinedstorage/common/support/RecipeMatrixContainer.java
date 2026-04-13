package com.refinedmods.refinedstorage.common.support;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.support.network.ResourceSorters;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.Comparator;
import java.util.List;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class RecipeMatrixContainer extends TransientCraftingContainer {
    @Nullable
    private final Runnable listener;
    private boolean muted;

    public RecipeMatrixContainer(@Nullable final Runnable listener, final int width, final int height) {
        super(new CraftingMatrixContainerMenu(listener), width, height);
        this.listener = listener;
    }

    public void changed() {
        if (listener != null) {
            listener.run();
        }
    }

    public boolean clearToPlayerInventory(final Player player) {
        boolean clearedAll = true;
        for (int i = 0; i < getContainerSize(); ++i) {
            final ItemStack matrixStack = getItem(i);
            if (matrixStack.isEmpty()) {
                continue;
            }
            if (player.getInventory().add(matrixStack)) {
                setItem(i, ItemStack.EMPTY);
            } else {
                clearedAll = false;
            }
        }
        return clearedAll;
    }

    public boolean clearIntoStorage(final RootStorage rootStorage, final Player player) {
        boolean clearedAll = true;
        for (int i = 0; i < getContainerSize(); ++i) {
            final ItemStack matrixStack = getItem(i);
            if (matrixStack.isEmpty()) {
                continue;
            }
            final ItemStack remainder = doInsert(matrixStack, player, rootStorage);
            if (!remainder.isEmpty()) {
                clearedAll = false;
            }
            setItem(i, remainder);
        }
        return clearedAll;
    }

    private ItemStack doInsert(final ItemStack stack,
                               final Player player,
                               final RootStorage rootStorage) {
        final long inserted = rootStorage.insert(
            ItemResource.ofItemStack(stack),
            stack.getCount(),
            Action.EXECUTE,
            new PlayerActor(player)
        );
        final long remainder = stack.getCount() - inserted;
        if (remainder == 0) {
            return ItemStack.EMPTY;
        }
        return stack.copyWithCount((int) remainder);
    }

    public void transferRecipe(final Player player,
                               @Nullable final RootStorage rootStorage,
                               final List<List<ItemResource>> recipe) {
        final boolean cleared = rootStorage == null
            ? clearToPlayerInventory(player)
            : clearIntoStorage(rootStorage, player);
        if (!cleared) {
            return;
        }
        final Comparator<ResourceKey> sorter = ResourceSorters.create(rootStorage, player.getInventory());
        for (int i = 0; i < getContainerSize(); ++i) {
            if (i >= recipe.size()) {
                break;
            }
            final List<ItemResource> possibilities = recipe.get(i);
            possibilities.sort(sorter);
            doTransferRecipe(i, possibilities, player, rootStorage);
        }
    }

    private void doTransferRecipe(final int index,
                                  final List<ItemResource> sortedPossibilities,
                                  final Player player,
                                  @Nullable final RootStorage rootStorage) {
        for (final ItemResource possibility : sortedPossibilities) {
            boolean extracted = rootStorage != null
                && rootStorage.extract(possibility, 1, Action.EXECUTE, new PlayerActor(player)) == 1;
            if (!extracted) {
                extracted = extractSingleItemFromPlayerInventory(player, possibility);
            }
            if (extracted) {
                setItem(index, possibility.toItemStack());
                return;
            }
        }
    }

    private boolean extractSingleItemFromPlayerInventory(final Player player, final ItemResource possibility) {
        final ItemStack possibilityStack = possibility.toItemStack();
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            final ItemStack playerStack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(playerStack, possibilityStack)) {
                player.getInventory().removeItem(i, 1);
                return true;
            }
        }
        return false;
    }

    public void updateMatrixAndNotifyListenerLater(final Runnable callback) {
        muted = true;
        try {
            callback.run();
        } finally {
            muted = false;
            changed();
        }
    }

    public boolean isMuted() {
        return muted;
    }
}
