package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.item;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.platform.fabric.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;

import static com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource.ofItemVariant;

public class ItemGridEventHandlerImpl implements ItemGridEventHandler {
    private final ScreenHandler screenHandler;
    private final GridService<ItemResource> gridService;
    private final PlayerInventoryStorage playerInventoryStorage;
    private final SingleSlotStorage<ItemVariant> playerCursorStorage;

    public ItemGridEventHandlerImpl(ScreenHandler screenHandler, GridService<ItemResource> gridService, PlayerInventory playerInventory) {
        this.screenHandler = screenHandler;
        this.gridService = gridService;
        this.playerInventoryStorage = PlayerInventoryStorage.of(playerInventory);
        this.playerCursorStorage = PlayerInventoryStorage.getCursorStorage(screenHandler);
    }

    @Override
    public void onInsert(GridInsertMode insertMode) {
        if (screenHandler.getCursorStack().isEmpty()) {
            return;
        }
        ItemResource itemResource = new ItemResource(screenHandler.getCursorStack());
        gridService.insert(itemResource, insertMode, (resource, amount, action) -> {
            try (Transaction tx = Transaction.openOuter()) {
                ItemVariant itemVariant = resource.toItemVariant();
                long extracted = playerCursorStorage.extract(itemVariant, amount, tx);
                if (action == Action.EXECUTE) {
                    tx.commit();
                }
                return extracted;
            }
        });
    }

    @Override
    public void onTransfer(int slotIndex) {
        SingleSlotStorage<ItemVariant> storage = playerInventoryStorage.getSlot(slotIndex);
        ItemVariant itemVariantInSlot = StorageUtil.findExtractableResource(storage, null);
        if (itemVariantInSlot == null) {
            return;
        }
        ItemResource itemResource = ofItemVariant(itemVariantInSlot);
        gridService.insert(itemResource, GridInsertMode.ENTIRE_RESOURCE, (resource, amount, action) -> {
            try (Transaction tx = Transaction.openOuter()) {
                ItemVariant itemVariant = resource.toItemVariant();
                long extracted = storage.extract(itemVariant, amount, tx);
                if (action == Action.EXECUTE) {
                    tx.commit();
                }
                return extracted;
            }
        });
    }

    @Override
    public void onExtract(ItemResource itemResource, GridExtractMode mode, boolean cursor) {
        gridService.extract(itemResource, mode, (resource, amount, action) -> {
            ItemVariant itemVariant = resource.toItemVariant();
            try (Transaction tx = Transaction.openOuter()) {
                long inserted = insert(itemVariant, amount, tx, cursor);
                long remainder = amount - inserted;
                if (action == Action.EXECUTE) {
                    tx.commit();
                }
                return remainder;
            }
        });
    }

    @Override
    public void onScroll(ItemResource itemResource, GridScrollMode mode, int slot) {
        Storage<ItemVariant> playerStorage = slot >= 0 ? playerInventoryStorage.getSlot(slot) : playerInventoryStorage;
        switch (mode) {
            case GRID_TO_INVENTORY -> handleGridToInventoryScroll(itemResource, playerStorage);
            case INVENTORY_TO_GRID -> handleInventoryToGridScroll(itemResource, playerStorage);
            case GRID_TO_CURSOR -> handleGridToInventoryScroll(itemResource, playerCursorStorage);
        }
    }

    private void handleInventoryToGridScroll(ItemResource itemResource, Storage<ItemVariant> sourceStorage) {
        gridService.insert(itemResource, GridInsertMode.SINGLE_RESOURCE, (resource, amount, action) -> {
            try (Transaction tx = Transaction.openOuter()) {
                ItemVariant itemVariant = resource.toItemVariant();
                long extracted = sourceStorage.extract(itemVariant, amount, tx);
                if (action == Action.EXECUTE) {
                    tx.commit();
                }
                return extracted;
            }
        });
    }

    private void handleGridToInventoryScroll(ItemResource itemResource, Storage<ItemVariant> destinationStorage) {
        gridService.extract(itemResource, GridExtractMode.SINGLE_RESOURCE, (resource, amount, action) -> {
            ItemVariant itemVariant = resource.toItemVariant();
            try (Transaction tx = Transaction.openOuter()) {
                long inserted = destinationStorage.insert(itemVariant, amount, tx);
                long remainder = amount - inserted;
                if (action == Action.EXECUTE) {
                    tx.commit();
                }
                return remainder;
            }
        });
    }

    private long insert(ItemVariant itemVariant, long amount, Transaction tx, boolean cursor) {
        Storage<ItemVariant> relevantStorage = cursor ? playerCursorStorage : playerInventoryStorage;
        return relevantStorage.insert(itemVariant, amount, tx);
    }
}
