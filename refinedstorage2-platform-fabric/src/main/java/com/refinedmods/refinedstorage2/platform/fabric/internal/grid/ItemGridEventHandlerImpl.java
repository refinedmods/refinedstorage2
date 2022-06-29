package com.refinedmods.refinedstorage2.platform.fabric.internal.grid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage2.platform.fabric.util.VariantUtil.ofItemVariant;
import static com.refinedmods.refinedstorage2.platform.fabric.util.VariantUtil.toItemVariant;

public class ItemGridEventHandlerImpl implements ItemGridEventHandler {
    private final AbstractContainerMenu containerMenu;
    private final GridService<ItemResource> gridService;
    private final PlayerInventoryStorage playerInventoryStorage;
    private final SingleSlotStorage<ItemVariant> playerCursorStorage;

    public ItemGridEventHandlerImpl(final AbstractContainerMenu containerMenu,
                                    final GridService<ItemResource> gridService,
                                    final Inventory playerInventory) {
        this.containerMenu = containerMenu;
        this.gridService = gridService;
        this.playerInventoryStorage = PlayerInventoryStorage.of(playerInventory);
        this.playerCursorStorage = PlayerInventoryStorage.getCursorStorage(containerMenu);
    }

    @Override
    public void onInsert(final GridInsertMode insertMode) {
        final ItemStack carried = containerMenu.getCarried();
        if (carried.isEmpty()) {
            return;
        }
        final ItemResource itemResource = new ItemResource(carried.getItem(), carried.getTag());
        gridService.insert(itemResource, insertMode, (resource, amount, action, source) -> {
            try (Transaction tx = Transaction.openOuter()) {
                final ItemVariant itemVariant = toItemVariant(resource);
                final long extracted = playerCursorStorage.extract(itemVariant, amount, tx);
                if (action == Action.EXECUTE) {
                    tx.commit();
                }
                return extracted;
            }
        });
    }

    @Override
    public void onTransfer(final int slotIndex) {
        final SingleSlotStorage<ItemVariant> storage = playerInventoryStorage.getSlot(slotIndex);
        final ItemVariant itemVariantInSlot = StorageUtil.findExtractableResource(storage, null);
        if (itemVariantInSlot == null) {
            return;
        }
        final ItemResource itemResource = ofItemVariant(itemVariantInSlot);
        gridService.insert(itemResource, GridInsertMode.ENTIRE_RESOURCE, (resource, amount, action, source) -> {
            try (Transaction tx = Transaction.openOuter()) {
                final ItemVariant itemVariant = toItemVariant(resource);
                final long extracted = storage.extract(itemVariant, amount, tx);
                if (action == Action.EXECUTE) {
                    tx.commit();
                }
                return extracted;
            }
        });
    }

    @Override
    public void onExtract(final ItemResource itemResource, final GridExtractMode mode, final boolean cursor) {
        gridService.extract(itemResource, mode, (resource, amount, action, source) -> {
            final ItemVariant itemVariant = toItemVariant(resource);
            try (Transaction tx = Transaction.openOuter()) {
                final long inserted = insert(itemVariant, amount, tx, cursor);
                if (action == Action.EXECUTE) {
                    tx.commit();
                }
                return inserted;
            }
        });
    }

    @Override
    public void onScroll(final ItemResource itemResource, final GridScrollMode mode, final int slotIndex) {
        final Storage<ItemVariant> playerStorage = slotIndex >= 0
                ? playerInventoryStorage.getSlot(slotIndex)
                : playerInventoryStorage;
        switch (mode) {
            case GRID_TO_INVENTORY -> handleGridToInventoryScroll(itemResource, playerStorage);
            case INVENTORY_TO_GRID -> handleInventoryToGridScroll(itemResource, playerStorage);
            case GRID_TO_CURSOR -> handleGridToInventoryScroll(itemResource, playerCursorStorage);
        }
    }

    private void handleInventoryToGridScroll(final ItemResource itemResource,
                                             final Storage<ItemVariant> sourceStorage) {
        gridService.insert(itemResource, GridInsertMode.SINGLE_RESOURCE, (resource, amount, action, source) -> {
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
        gridService.extract(itemResource, GridExtractMode.SINGLE_RESOURCE, (resource, amount, action, source) -> {
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

    private long insert(final ItemVariant itemVariant, final long amount, final Transaction tx, final boolean cursor) {
        final Storage<ItemVariant> relevantStorage = cursor ? playerCursorStorage : playerInventoryStorage;
        return relevantStorage.insert(itemVariant, amount, tx);
    }
}
