package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.item;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;

import java.util.Optional;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.impl.transfer.item.ItemVariantImpl;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
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
    public void insert(GridInsertMode insertMode) {
        if (screenHandler.getCursorStack().isEmpty()) {
            return;
        }
        ResourceAmount<ItemResource> toInsert = Rs2PlatformApiFacade.INSTANCE.toItemResourceAmount(screenHandler.getCursorStack());
        Optional<ResourceAmount<ItemResource>> remainder = gridService.insert(toInsert, insertMode);
        screenHandler.setCursorStack(remainder.map(Rs2PlatformApiFacade.INSTANCE::toItemStack).orElse(ItemStack.EMPTY));
    }

    @Override
    public ItemStack transfer(ItemStack stack) {
        ResourceAmount<ItemResource> toInsert = Rs2PlatformApiFacade.INSTANCE.toItemResourceAmount(stack);
        Optional<ResourceAmount<ItemResource>> remainder = gridService.insert(toInsert, GridInsertMode.ENTIRE_RESOURCE);
        return remainder.map(Rs2PlatformApiFacade.INSTANCE::toItemStack).orElse(ItemStack.EMPTY);
    }

    @Override
    public void onExtract(ItemResource itemResource, GridExtractMode mode, boolean cursor) {
        gridService.extract(itemResource, mode, (resource, amount, action) -> {
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
        ItemVariant itemVariant = ItemVariant.of(itemResource.getItem(), itemResource.getTag());
        try (Transaction tx = Transaction.openOuter()) {
            long extracted = sourceStorage.extract(itemVariant, 1, tx);
            if (extracted > 0) {
                Optional<ResourceAmount<ItemResource>> remainder = gridService.insert(
                        new ResourceAmount<>(itemResource, extracted),
                        GridInsertMode.ENTIRE_RESOURCE
                );
                remainder.ifPresent(remainderResourceAmount -> insert(
                        ItemVariantImpl.of(remainderResourceAmount.getResource().getItem(), remainderResourceAmount.getResource().getTag()),
                        remainderResourceAmount.getAmount(),
                        tx,
                        sourceStorage
                ));
                tx.commit();
            }
        }
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

    private long insert(ItemVariant itemVariant, long amount, Transaction tx, Storage<ItemVariant> targetStorage) {
        if (targetStorage instanceof PlayerInventoryStorage) {
            return ((PlayerInventoryStorage) targetStorage).offer(itemVariant, amount, tx);
        }
        return targetStorage.insert(itemVariant, amount, tx);
    }
}
