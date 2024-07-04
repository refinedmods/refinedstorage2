package com.refinedmods.refinedstorage.platform.neoforge.grid.strategy;

import com.refinedmods.refinedstorage.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage.platform.api.exporter.AmountOverride;
import com.refinedmods.refinedstorage.platform.api.grid.Grid;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridInsertionStrategy;
import com.refinedmods.refinedstorage.platform.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceTypes;
import com.refinedmods.refinedstorage.platform.neoforge.storage.CapabilityCache;
import com.refinedmods.refinedstorage.platform.neoforge.storage.ItemHandlerExtractableStorage;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

import static com.refinedmods.refinedstorage.platform.common.support.resource.ItemResource.ofItemStack;

public class ItemGridInsertionStrategy implements GridInsertionStrategy {
    private final AbstractContainerMenu containerMenu;
    private final GridOperations gridOperations;
    private final CursorItemHandler playerCursorItemHandler;

    public ItemGridInsertionStrategy(final AbstractContainerMenu containerMenu,
                                     final ServerPlayer player,
                                     final Grid grid) {
        this.containerMenu = containerMenu;
        this.gridOperations = grid.createOperations(ResourceTypes.ITEM, player);
        this.playerCursorItemHandler = new CursorItemHandler(containerMenu);
    }

    @Override
    public boolean onInsert(final GridInsertMode insertMode, final boolean tryAlternatives) {
        final ItemStack carried = containerMenu.getCarried();
        if (carried.isEmpty()) {
            return false;
        }
        final ItemResource itemResource = ItemResource.ofItemStack(carried);
        gridOperations.insert(
            itemResource,
            insertMode,
            new ItemHandlerExtractableStorage(
                CapabilityCache.ofItemHandler(playerCursorItemHandler),
                AmountOverride.NONE
            )
        );
        return true;
    }

    @Override
    public boolean onTransfer(final int slotIndex) {
        final Slot slot = containerMenu.getSlot(slotIndex);
        final RangedWrapper storage = new RangedWrapper(
            new InvWrapper(slot.container),
            slot.getContainerSlot(),
            slot.getContainerSlot() + 1
        );
        final ItemStack itemStackInSlot = storage.getStackInSlot(0);
        if (itemStackInSlot.isEmpty()) {
            return false;
        }
        final ItemResource itemResource = ofItemStack(itemStackInSlot);
        gridOperations.insert(
            itemResource,
            GridInsertMode.ENTIRE_RESOURCE,
            new ItemHandlerExtractableStorage(
                CapabilityCache.ofItemHandler(storage),
                AmountOverride.NONE
            )
        );
        return true;
    }
}
