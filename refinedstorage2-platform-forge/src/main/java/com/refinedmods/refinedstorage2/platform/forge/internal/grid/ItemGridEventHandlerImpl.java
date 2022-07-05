package com.refinedmods.refinedstorage2.platform.forge.internal.grid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;

import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.ofItemStack;
import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.toItemStack;

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
        gridService.insert(itemResource, insertMode, (resource, amount, action, source) -> {
            final ItemStack extracted = playerCursorStorage.extractItem(
                0,
                (int) amount,
                action == Action.SIMULATE
            );
            return extracted.getCount();
        });
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
            (resource, amount, action, source) -> extract(storage, resource, amount, action)
        );
    }

    @Override
    public void onExtract(final ItemResource itemResource, final GridExtractMode mode, final boolean cursor) {
        gridService.extract(itemResource, mode, (resource, amount, action, source) -> {
            final ItemStack toInsert = toItemStack(resource, amount);
            return insert(toInsert, action, cursor);
        });
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
            (resource, amount, action, source) -> extract(sourceStorage, resource, amount, action)
        );
    }

    private void handleGridToInventoryScroll(final ItemResource itemResource, final IItemHandler destinationStorage) {
        gridService.extract(itemResource, GridExtractMode.SINGLE_RESOURCE, (resource, amount, action, source) -> {
            final ItemStack toInsert = toItemStack(resource, amount);
            final ItemStack remainder = ItemHandlerHelper.insertItem(
                destinationStorage,
                toInsert,
                action == Action.SIMULATE
            );
            return amount - remainder.getCount();
        });
    }

    private long insert(final ItemStack itemStack, final Action action, final boolean cursor) {
        final IItemHandler handler = cursor ? playerCursorStorage : playerInventoryStorage;
        final ItemStack remainder = ItemHandlerHelper.insertItem(handler, itemStack, action == Action.SIMULATE);
        return (long) itemStack.getCount() - remainder.getCount();
    }

    private long extract(final IItemHandler source,
                         final ItemResource template,
                         final long amount,
                         final Action action) {
        final ItemStack toExtractStack = toItemStack(template, amount);
        long extracted = 0;
        for (int slot = 0; slot < source.getSlots(); ++slot) {
            final boolean relevant = isSame(source.getStackInSlot(slot), toExtractStack);
            if (!relevant) {
                continue;
            }
            final long toExtract = amount - extracted;
            extracted += source.extractItem(slot, (int) toExtract, action == Action.SIMULATE).getCount();
            if (extracted >= amount) {
                break;
            }
        }
        return extracted;
    }

    private boolean isSame(final ItemStack a, final ItemStack b) {
        return ItemStack.isSameItemSameTags(a, b);
    }
}
