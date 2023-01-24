package com.refinedmods.refinedstorage2.platform.forge.internal.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.grid.service.GridServiceFactory;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.AbstractItemGridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.InteractionCoordinates;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.ItemHandlerExtractableStorage;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;

import static com.refinedmods.refinedstorage2.platform.api.resource.ItemResource.ofItemStack;

public class ItemGridInsertionStrategy extends AbstractItemGridInsertionStrategy {
    private final AbstractContainerMenu containerMenu;
    private final GridService<ItemResource> gridService;
    private final Inventory playerInventory;
    private final CursorStorage playerCursorStorage;

    public ItemGridInsertionStrategy(final AbstractContainerMenu containerMenu,
                                     final Player player,
                                     final GridServiceFactory gridServiceFactory) {
        this.containerMenu = containerMenu;
        this.gridService = createGridService(player, gridServiceFactory);
        this.playerInventory = player.getInventory();
        this.playerCursorStorage = new CursorStorage(containerMenu);
    }

    @Override
    public boolean onInsert(final GridInsertMode insertMode, final boolean tryAlternatives) {
        final ItemStack carried = containerMenu.getCarried();
        if (carried.isEmpty()) {
            return false;
        }
        final ItemResource itemResource = new ItemResource(carried.getItem(), carried.getTag());
        gridService.insert(
            itemResource,
            insertMode,
            new ItemHandlerExtractableStorage(InteractionCoordinates.ofItemHandler(playerCursorStorage))
        );
        return true;
    }

    @Override
    public boolean onTransfer(final int slotIndex) {
        final RangedWrapper storage = new RangedWrapper(
            new InvWrapper(playerInventory),
            slotIndex,
            slotIndex + 1
        );
        final ItemStack itemStackInSlot = storage.getStackInSlot(0);
        if (itemStackInSlot.isEmpty()) {
            return false;
        }
        final ItemResource itemResource = ofItemStack(itemStackInSlot);
        gridService.insert(
            itemResource,
            GridInsertMode.ENTIRE_RESOURCE,
            new ItemHandlerExtractableStorage(InteractionCoordinates.ofItemHandler(storage))
        );
        return true;
    }
}
