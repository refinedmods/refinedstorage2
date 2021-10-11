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
import net.fabricmc.fabric.impl.transfer.item.ItemVariantImpl;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;

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
                ItemVariant itemVariant = ItemVariant.of(resource.getItem(), resource.getTag());
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
        ItemResource itemResource = new ItemResource(itemVariantInSlot.getItem(), itemVariantInSlot.getNbt());
        gridService.insert(itemResource, GridInsertMode.ENTIRE_RESOURCE, (resource, amount, action) -> {
            try (Transaction tx = Transaction.openOuter()) {
                ItemVariant itemVariant = ItemVariant.of(resource.getItem(), resource.getTag());
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
            // todo: create util
            ItemVariant itemVariant = ItemVariantImpl.of(resource.getItem(), resource.getTag());
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
                ItemVariant itemVariant = ItemVariant.of(resource.getItem(), resource.getTag());
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
            ItemVariant itemVariant = ItemVariant.of(resource.getItem(), resource.getTag());
            try (Transaction tx = Transaction.openOuter()) {
                long inserted = insert(itemVariant, amount, tx, destinationStorage);
                long remainder = amount - inserted;
                if (action == Action.EXECUTE) {
                    tx.commit();
                }
                return remainder;
            }
        });
    }

    private long insert(ItemVariant itemVariant, long amount, Transaction tx, boolean cursor) {
        return insert(itemVariant, amount, tx, cursor ? playerCursorStorage : playerInventoryStorage);
    }

    // TODO: remove when upgrading Fabric
    private long insert(ItemVariant itemVariant, long amount, Transaction tx, Storage<ItemVariant> targetStorage) {
        if (targetStorage instanceof PlayerInventoryStorage) {
            return ((PlayerInventoryStorage) targetStorage).offer(itemVariant, amount, tx);
        }
        return targetStorage.insert(itemVariant, amount, tx);
    }
}
