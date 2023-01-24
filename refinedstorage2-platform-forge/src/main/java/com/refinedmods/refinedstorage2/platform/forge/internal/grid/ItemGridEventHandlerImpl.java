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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;

public class ItemGridEventHandlerImpl implements ItemGridEventHandler {
    private final GridService<ItemResource> gridService;
    private final Inventory playerInventory;
    private final PlayerMainInvWrapper playerInventoryStorage;
    private final CursorStorage playerCursorStorage;

    public ItemGridEventHandlerImpl(final AbstractContainerMenu containerMenu,
                                    final GridService<ItemResource> gridService,
                                    final Inventory playerInventory) {
        this.gridService = gridService;
        this.playerInventory = playerInventory;
        this.playerInventoryStorage = new PlayerMainInvWrapper(playerInventory);
        this.playerCursorStorage = new CursorStorage(containerMenu);
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
