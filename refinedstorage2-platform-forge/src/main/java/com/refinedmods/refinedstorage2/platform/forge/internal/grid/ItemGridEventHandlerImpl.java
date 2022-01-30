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
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;

import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.ofItemStack;
import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.toItemStack;

public class ItemGridEventHandlerImpl implements ItemGridEventHandler {
    private final AbstractContainerMenu screenHandler;
    private final GridService<ItemResource> gridService;
    private final Inventory playerInventory;

    public ItemGridEventHandlerImpl(AbstractContainerMenu screenHandler, GridService<ItemResource> gridService, Inventory playerInventory) {
        this.screenHandler = screenHandler;
        this.gridService = gridService;
        this.playerInventory = playerInventory;
    }

    @Override
    public void onInsert(GridInsertMode insertMode) {
        if (screenHandler.getCarried().isEmpty()) {
            return;
        }
        ItemResource itemResource = new ItemResource(screenHandler.getCarried());
        gridService.insert(itemResource, insertMode, (resource, amount, action) -> {
            ItemStack requested = toItemStack(resource);
            if (screenHandler.getCarried().isEmpty()) {
                return 0;
            }
            boolean same = isSame(requested, screenHandler.getCarried());
            if (!same) {
                return 0;
            }
            long extracted = Math.min(requested.getCount(), amount);
            if (action == Action.EXECUTE) {
                screenHandler.getCarried().shrink((int) extracted);
            }
            return extracted;
        });
    }

    @Override
    public void onTransfer(int slotIndex) {
        RangedWrapper storage = new RangedWrapper(new InvWrapper(playerInventory), slotIndex, slotIndex + 1);
        ItemStack itemStackInSlot = storage.getStackInSlot(0);
        if (itemStackInSlot.isEmpty()) {
            return;
        }
        ItemResource itemResource = ofItemStack(itemStackInSlot);
        gridService.insert(itemResource, GridInsertMode.ENTIRE_RESOURCE, (resource, amount, action) -> {
            ItemStack requested = toItemStack(resource);
            ItemStack inSlot = storage.getStackInSlot(0);
            if (!isSame(requested, inSlot)) {
                return 0;
            }
            return storage.extractItem(0, (int) amount, action == Action.SIMULATE).getCount();
        });
    }

    @Override
    public void onExtract(ItemResource itemResource, GridExtractMode mode, boolean cursor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onScroll(ItemResource itemResource, GridScrollMode mode, int slot) {
        throw new UnsupportedOperationException();
    }

    private boolean isSame(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameTags(a, b);
    }
}
