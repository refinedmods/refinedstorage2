package com.refinedmods.refinedstorage2.platform.fabric.grid.strategy;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static com.refinedmods.refinedstorage2.platform.fabric.support.resource.VariantUtil.toItemVariant;

public class ItemGridScrollingStrategy implements GridScrollingStrategy {
    private final GridOperations<ItemResource> gridOperations;
    private final PlayerInventoryStorage playerInventoryStorage;
    private final SingleSlotStorage<ItemVariant> playerCursorStorage;

    public ItemGridScrollingStrategy(final AbstractContainerMenu containerMenu,
                                     final Player player,
                                     final Grid grid) {
        this.gridOperations = grid.createOperations(StorageChannelTypes.ITEM, new PlayerActor(player));
        this.playerInventoryStorage = PlayerInventoryStorage.of(player.getInventory());
        this.playerCursorStorage = PlayerInventoryStorage.getCursorStorage(containerMenu);
    }

    @Override
    public <T> boolean onScroll(
        final PlatformStorageChannelType<T> storageChannelType,
        final T resource,
        final GridScrollMode scrollMode,
        final int slotIndex
    ) {
        if (resource instanceof ItemResource itemResource) {
            final Storage<ItemVariant> playerStorage = slotIndex >= 0
                ? playerInventoryStorage.getSlot(slotIndex)
                : playerInventoryStorage;
            switch (scrollMode) {
                case GRID_TO_INVENTORY -> handleGridToInventoryScroll(itemResource, playerStorage);
                case INVENTORY_TO_GRID -> handleInventoryToGridScroll(itemResource, playerStorage);
                case GRID_TO_CURSOR -> handleGridToInventoryScroll(itemResource, playerCursorStorage);
            }
            return true;
        }
        return false;
    }

    private void handleInventoryToGridScroll(final ItemResource itemResource,
                                             final Storage<ItemVariant> sourceStorage) {
        gridOperations.insert(itemResource, GridInsertMode.SINGLE_RESOURCE, (resource, amount, action, source) -> {
            try (Transaction tx = Transaction.openOuter()) {
                final ItemVariant itemVariant = toItemVariant(resource);
                final long extracted = sourceStorage.extract(itemVariant, amount, tx);
                if (action == Action.EXECUTE) {
                    tx.commit();
                }
                return extracted;
            }
        });
    }

    private void handleGridToInventoryScroll(final ItemResource itemResource,
                                             final Storage<ItemVariant> destinationStorage) {
        gridOperations.extract(itemResource, GridExtractMode.SINGLE_RESOURCE, (resource, amount, action, source) -> {
            final ItemVariant itemVariant = toItemVariant(resource);
            try (Transaction tx = Transaction.openOuter()) {
                final long inserted = destinationStorage.insert(itemVariant, amount, tx);
                if (action == Action.EXECUTE) {
                    tx.commit();
                }
                return inserted;
            }
        });
    }
}
