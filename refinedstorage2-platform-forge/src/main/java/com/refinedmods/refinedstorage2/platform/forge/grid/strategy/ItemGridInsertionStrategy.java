package com.refinedmods.refinedstorage2.platform.forge.grid.strategy;

import com.refinedmods.refinedstorage2.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage2.platform.api.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.forge.grid.CursorStorage;
import com.refinedmods.refinedstorage2.platform.forge.storage.InteractionCoordinates;
import com.refinedmods.refinedstorage2.platform.forge.storage.ItemHandlerExtractableStorage;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;

import static com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource.ofItemStack;

public class ItemGridInsertionStrategy implements GridInsertionStrategy {
    private final AbstractContainerMenu containerMenu;
    private final GridOperations<ItemResource> gridOperations;
    private final CursorStorage playerCursorStorage;

    public ItemGridInsertionStrategy(final AbstractContainerMenu containerMenu,
                                     final Player player,
                                     final Grid grid) {
        this.containerMenu = containerMenu;
        this.gridOperations = grid.createOperations(StorageChannelTypes.ITEM, new PlayerActor(player));
        this.playerCursorStorage = new CursorStorage(containerMenu);
    }

    @Override
    public boolean onInsert(final GridInsertMode insertMode, final boolean tryAlternatives) {
        final ItemStack carried = containerMenu.getCarried();
        if (carried.isEmpty()) {
            return false;
        }
        final ItemResource itemResource = new ItemResource(carried.getItem(), carried.getTag());
        gridOperations.insert(
            itemResource,
            insertMode,
            new ItemHandlerExtractableStorage(
                InteractionCoordinates.ofItemHandler(playerCursorStorage),
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
                InteractionCoordinates.ofItemHandler(storage),
                AmountOverride.NONE
            )
        );
        return true;
    }
}
