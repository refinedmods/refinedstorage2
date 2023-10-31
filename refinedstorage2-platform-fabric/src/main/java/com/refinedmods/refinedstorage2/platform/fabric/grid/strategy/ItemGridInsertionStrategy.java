package com.refinedmods.refinedstorage2.platform.fabric.grid.strategy;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage2.platform.fabric.support.resource.VariantUtil.ofItemVariant;
import static com.refinedmods.refinedstorage2.platform.fabric.support.resource.VariantUtil.toItemVariant;

public class ItemGridInsertionStrategy implements GridInsertionStrategy {
    private final AbstractContainerMenu containerMenu;
    private final GridOperations<ItemResource> gridOperations;
    private final SingleSlotStorage<ItemVariant> playerCursorStorage;

    public ItemGridInsertionStrategy(final AbstractContainerMenu containerMenu,
                                     final Player player,
                                     final Grid grid) {
        this.containerMenu = containerMenu;
        this.gridOperations = grid.createOperations(StorageChannelTypes.ITEM, new PlayerActor(player));
        this.playerCursorStorage = PlayerInventoryStorage.getCursorStorage(containerMenu);
    }

    @Override
    public boolean onInsert(final GridInsertMode insertMode, final boolean tryAlternatives) {
        final ItemStack carried = containerMenu.getCarried();
        if (carried.isEmpty()) {
            return false;
        }
        final ItemResource itemResource = new ItemResource(carried.getItem(), carried.getTag());
        gridOperations.insert(itemResource, insertMode, (resource, amount, action, source) -> {
            try (Transaction tx = Transaction.openOuter()) {
                final ItemVariant itemVariant = toItemVariant(resource);
                final long extracted = playerCursorStorage.extract(itemVariant, amount, tx);
                if (action == Action.EXECUTE) {
                    tx.commit();
                }
                return extracted;
            }
        });
        return true;
    }

    @Override
    public boolean onTransfer(final int slotIndex) {
        final Slot slot = containerMenu.getSlot(slotIndex);
        final InventoryStorage inventoryStorage = InventoryStorage.of(slot.container, null);
        final SingleSlotStorage<ItemVariant> storage = inventoryStorage.getSlot(slot.getContainerSlot());
        final ItemVariant itemVariantInSlot = StorageUtil.findExtractableResource(storage, null);
        if (itemVariantInSlot == null) {
            return false;
        }
        final ItemResource itemResource = ofItemVariant(itemVariantInSlot);
        gridOperations.insert(itemResource, GridInsertMode.ENTIRE_RESOURCE, (resource, amount, action, source) -> {
            try (Transaction tx = Transaction.openOuter()) {
                final ItemVariant itemVariant = toItemVariant(resource);
                final long extracted = storage.extract(itemVariant, amount, tx);
                if (action == Action.EXECUTE) {
                    tx.commit();
                }
                return extracted;
            }
        });
        return true;
    }
}
