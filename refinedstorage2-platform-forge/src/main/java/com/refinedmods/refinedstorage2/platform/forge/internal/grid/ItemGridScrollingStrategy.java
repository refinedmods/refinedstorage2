package com.refinedmods.refinedstorage2.platform.forge.internal.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.PlatformGridServiceFactory;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.InteractionCoordinates;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.ItemHandlerExtractableStorage;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.ItemHandlerInsertableStorage;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;

public class ItemGridScrollingStrategy implements GridScrollingStrategy {
    private final GridService<ItemResource> gridService;
    private final Inventory playerInventory;
    private final PlayerMainInvWrapper playerInventoryStorage;
    private final CursorStorage playerCursorStorage;

    public ItemGridScrollingStrategy(final AbstractContainerMenu containerMenu,
                                     final Player player,
                                     final PlatformGridServiceFactory gridServiceFactory) {
        this.gridService = gridServiceFactory.createForItem(new PlayerActor(player));
        this.playerInventory = player.getInventory();
        this.playerInventoryStorage = new PlayerMainInvWrapper(playerInventory);
        this.playerCursorStorage = new CursorStorage(containerMenu);
    }

    @Override
    public <T> boolean onScroll(final PlatformStorageChannelType<T> storageChannelType,
                                final T resource,
                                final GridScrollMode scrollMode,
                                final int slotIndex) {
        if (resource instanceof ItemResource itemResource) {
            final IItemHandler playerStorage = slotIndex >= 0
                ? new RangedWrapper(new InvWrapper(playerInventory), slotIndex, slotIndex + 1)
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
