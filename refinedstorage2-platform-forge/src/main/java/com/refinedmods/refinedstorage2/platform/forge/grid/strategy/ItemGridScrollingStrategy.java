package com.refinedmods.refinedstorage2.platform.forge.grid.strategy;

import com.refinedmods.refinedstorage2.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage2.platform.api.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.forge.storage.CapabilityCache;
import com.refinedmods.refinedstorage2.platform.forge.storage.ItemHandlerExtractableStorage;
import com.refinedmods.refinedstorage2.platform.forge.storage.ItemHandlerInsertableStorage;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

public class ItemGridScrollingStrategy implements GridScrollingStrategy {
    private final GridOperations<ItemResource> gridOperations;
    private final Inventory playerInventory;
    private final PlayerMainInvWrapper playerInventoryStorage;
    private final CursorItemHandler playerCursorItemHandler;

    public ItemGridScrollingStrategy(final AbstractContainerMenu containerMenu,
                                     final Player player,
                                     final Grid grid) {
        this.gridOperations = grid.createOperations(StorageChannelTypes.ITEM, new PlayerActor(player));
        this.playerInventory = player.getInventory();
        this.playerInventoryStorage = new PlayerMainInvWrapper(playerInventory);
        this.playerCursorItemHandler = new CursorItemHandler(containerMenu);
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
                case GRID_TO_CURSOR -> handleGridToInventoryScroll(itemResource, playerCursorItemHandler);
            }
            return true;
        }
        return false;
    }

    private void handleInventoryToGridScroll(final ItemResource itemResource, final IItemHandler sourceStorage) {
        gridOperations.insert(
            itemResource,
            GridInsertMode.SINGLE_RESOURCE,
            new ItemHandlerExtractableStorage(
                CapabilityCache.ofItemHandler(sourceStorage),
                AmountOverride.NONE
            )
        );
    }

    private void handleGridToInventoryScroll(final ItemResource itemResource, final IItemHandler destinationStorage) {
        gridOperations.extract(
            itemResource,
            GridExtractMode.SINGLE_RESOURCE,
            new ItemHandlerInsertableStorage(
                CapabilityCache.ofItemHandler(destinationStorage),
                AmountOverride.NONE
            )
        );
    }
}
