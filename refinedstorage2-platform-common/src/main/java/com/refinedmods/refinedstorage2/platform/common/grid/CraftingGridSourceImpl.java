package com.refinedmods.refinedstorage2.platform.common.grid;

import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;

import java.util.Comparator;
import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;

class CraftingGridSourceImpl implements CraftingGridSource {
    private final CraftingGridBlockEntity blockEntity;

    CraftingGridSourceImpl(final CraftingGridBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public CraftingMatrix getCraftingMatrix() {
        return blockEntity.getCraftingMatrix();
    }

    @Override
    public ResultContainer getCraftingResult() {
        return blockEntity.getCraftingResult();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(final Player player) {
        return blockEntity.getRemainingItems(player);
    }

    @Override
    public CraftingGridRefillContext openRefillContext() {
        return new CraftingGridRefillContextImpl(blockEntity);
    }

    @Override
    public CraftingGridRefillContext openSnapshotRefillContext(final Player player) {
        return new SnapshotCraftingGridRefillContext(player, blockEntity, blockEntity.getCraftingMatrix());
    }

    @Override
    public void acceptQuickCraft(final Player player, final ItemStack craftedStack) {
        if (player.getInventory().add(craftedStack)) {
            return;
        }
        final ItemStack remainder = blockEntity.insert(craftedStack, player);
        if (!remainder.isEmpty()) {
            player.drop(remainder, false);
        }
    }

    @Override
    public boolean clearMatrix(final Player player, final boolean toPlayerInventory) {
        boolean success = true;
        for (int i = 0; i < getCraftingMatrix().getContainerSize(); ++i) {
            final ItemStack matrixStack = getCraftingMatrix().getItem(i);
            if (matrixStack.isEmpty()) {
                continue;
            }
            if (toPlayerInventory) {
                if (player.getInventory().add(matrixStack)) {
                    getCraftingMatrix().setItem(i, ItemStack.EMPTY);
                } else {
                    success = false;
                }
            } else {
                final ItemStack remainder = blockEntity.insert(matrixStack, player);
                if (!remainder.isEmpty()) {
                    success = false;
                }
                getCraftingMatrix().setItem(i, remainder);
            }
        }
        return success;
    }

    @Override
    public void transferRecipe(final Player player, final List<List<ItemResource>> recipe) {
        final boolean clearToPlayerInventory = blockEntity.getNetwork().isEmpty();
        if (!clearMatrix(player, clearToPlayerInventory)) {
            return;
        }
        final ResourceList available = createCombinedPlayerInventoryAndNetworkList(player);
        final Comparator<ItemResource> sorter = sortByHighestAvailableFirst(available);
        for (int i = 0; i < getCraftingMatrix().getContainerSize(); ++i) {
            if (i > recipe.size() || recipe.get(i) == null) {
                continue;
            }
            final List<ItemResource> possibilities = recipe.get(i);
            possibilities.sort(sorter);
            doTransferRecipe(i, possibilities, player);
        }
    }

    private void doTransferRecipe(final int index, final List<ItemResource> sortedPossibilities, final Player player) {
        for (final ItemResource possibility : sortedPossibilities) {
            boolean extracted = blockEntity.extract(possibility, player) == 1;
            if (!extracted) {
                extracted = extractFromPlayerInventory(player, possibility);
            }
            if (extracted) {
                getCraftingMatrix().setItem(index, possibility.toItemStack());
                return;
            }
        }
    }

    private boolean extractFromPlayerInventory(final Player player, final ItemResource possibility) {
        final ItemStack possibilityStack = possibility.toItemStack();
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            final ItemStack playerStack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameTags(playerStack, possibilityStack)) {
                player.getInventory().removeItem(i, 1);
                return true;
            }
        }
        return false;
    }

    private ResourceList createCombinedPlayerInventoryAndNetworkList(final Player player) {
        final ResourceList list = new ResourceListImpl();
        addNetworkItemsIntoList(list);
        addPlayerInventoryItemsIntoList(player, list);
        return list;
    }

    private void addNetworkItemsIntoList(final ResourceList list) {
        blockEntity.getNetwork().ifPresent(network -> network.getComponent(StorageNetworkComponent.class)
            .getStorageChannel(StorageChannelTypes.ITEM)
            .getAll()
            .forEach(list::add));
    }

    private void addPlayerInventoryItemsIntoList(final Player player, final ResourceList list) {
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            final ItemStack playerInventoryStack = player.getInventory().getItem(i);
            if (playerInventoryStack.isEmpty()) {
                continue;
            }
            list.add(ItemResource.ofItemStack(playerInventoryStack), playerInventoryStack.getCount());
        }
    }

    private Comparator<ItemResource> sortByHighestAvailableFirst(final ResourceList available) {
        return Comparator.<ItemResource>comparingLong(resource -> getAvailableAmount(available, resource)).reversed();
    }

    private long getAvailableAmount(final ResourceList available, final ItemResource resource) {
        return available.get(resource).map(ResourceAmount::getAmount).orElse(0L);
    }
}
