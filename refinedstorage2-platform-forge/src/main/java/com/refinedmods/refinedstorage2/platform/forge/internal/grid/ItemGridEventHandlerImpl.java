package com.refinedmods.refinedstorage2.platform.forge.internal.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.InteractionCoordinates;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.ItemHandlerExtractableStorage;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.ItemHandlerInsertableStorage;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;

import static com.refinedmods.refinedstorage2.platform.api.resource.ItemResource.ofItemStack;

public class ItemGridEventHandlerImpl implements ItemGridEventHandler {
    private final AbstractContainerMenu containerMenu;
    private final GridService<ItemResource> gridService;
    private final Inventory playerInventory;
    private final PlayerMainInvWrapper playerInventoryStorage;
    private final CursorStorage playerCursorStorage;

    public ItemGridEventHandlerImpl(final AbstractContainerMenu containerMenu,
                                    final GridService<ItemResource> gridService,
                                    final Inventory playerInventory) {
        this.containerMenu = containerMenu;
        this.gridService = gridService;
        this.playerInventory = playerInventory;
        this.playerInventoryStorage = new PlayerMainInvWrapper(playerInventory);
        this.playerCursorStorage = new CursorStorage(containerMenu);
    }

    @Override
    public void onInsert(final GridInsertMode insertMode) {
        final ItemStack carried = containerMenu.getCarried();
        if (carried.isEmpty()) {
            return;
        }
        final ItemResource itemResource = new ItemResource(carried.getItem(), carried.getTag());
        gridService.insert(
            itemResource,
            insertMode,
            new ItemHandlerExtractableStorage(InteractionCoordinates.ofItemHandler(playerCursorStorage))
        );
    }

    @Override
    public void onTransfer(final int slotIndex) {
        final RangedWrapper storage = new RangedWrapper(
            new InvWrapper(playerInventory),
            slotIndex,
            slotIndex + 1
        );
        final ItemStack itemStackInSlot = storage.getStackInSlot(0);
        if (itemStackInSlot.isEmpty()) {
            return;
        }
        final ItemResource itemResource = ofItemStack(itemStackInSlot);
        gridService.insert(
            itemResource,
            GridInsertMode.ENTIRE_RESOURCE,
            new ItemHandlerExtractableStorage(InteractionCoordinates.ofItemHandler(storage))
        );
    }

    @Override
    public void onExtract(final ItemResource itemResource, final GridExtractMode mode, final boolean cursor) {
        final IItemHandler handler = cursor ? playerCursorStorage : playerInventoryStorage;
        gridService.extract(
            itemResource,
            mode,
            new ItemHandlerInsertableStorage(InteractionCoordinates.ofItemHandler(handler))
        );
    }

    @Override
    public void onScroll(final ItemResource itemResource, final GridScrollMode mode, final int slotIndex) {
        final IItemHandler playerStorage = slotIndex >= 0
            ? new RangedWrapper(new InvWrapper(playerInventory), slotIndex, slotIndex + 1)
            : playerInventoryStorage;

        switch (mode) {
            case GRID_TO_INVENTORY -> handleGridToInventoryScroll(itemResource, playerStorage);
            case INVENTORY_TO_GRID -> handleInventoryToGridScroll(itemResource, playerStorage);
            case GRID_TO_CURSOR -> handleGridToInventoryScroll(itemResource, playerCursorStorage);
        }
    }

    private void handleInventoryToGridScroll(final ItemResource itemResource, final IItemHandler sourceStorage) {
        gridService.insert(
            itemResource,
            GridInsertMode.SINGLE_RESOURCE,
            new ItemHandlerExtractableStorage(InteractionCoordinates.ofItemHandler(sourceStorage))
        );
    }

    private void handleGridToInventoryScroll(final ItemResource itemResource, final IItemHandler destinationStorage) {
        gridService.extract(
            itemResource,
            GridExtractMode.SINGLE_RESOURCE,
            new ItemHandlerInsertableStorage(InteractionCoordinates.ofItemHandler(destinationStorage))
        );
    }
}
