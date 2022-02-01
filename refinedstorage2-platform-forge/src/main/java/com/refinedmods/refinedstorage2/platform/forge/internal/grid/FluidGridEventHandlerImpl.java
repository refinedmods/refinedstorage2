package com.refinedmods.refinedstorage2.platform.forge.internal.grid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.platform.api.grid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;

import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.ofFluidStack;
import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.toFluidStack;

public class FluidGridEventHandlerImpl implements FluidGridEventHandler {
    private final AbstractContainerMenu screenHandler;
    private final Inventory playerInventory;
    private final GridService<FluidResource> gridService;
    private final PlayerMainInvWrapper playerInventoryStorage;
    private final CursorStorage playerCursorStorage;

    public FluidGridEventHandlerImpl(AbstractContainerMenu screenHandler, Inventory playerInventory, GridService<FluidResource> gridService) {
        this.screenHandler = screenHandler;
        this.playerInventory = playerInventory;
        this.gridService = gridService;
        this.playerInventoryStorage = new PlayerMainInvWrapper(playerInventory);
        this.playerCursorStorage = new CursorStorage(screenHandler);
    }

    @Override
    public void onInsert(GridInsertMode insertMode) {
        IFluidHandlerItem cursorStorage = getFluidCursorStorage();
        if (cursorStorage == null) {
            return;
        }
        FluidStack extractableResource = cursorStorage.getFluidInTank(0);
        if (extractableResource == null) {
            return;
        }
        FluidResource fluidResource = ofFluidStack(extractableResource);
        gridService.insert(fluidResource, insertMode, (resource, amount, action) -> {
            FluidStack toDrain = toFluidStack(resource, amount == Long.MAX_VALUE ? Integer.MAX_VALUE : amount);
            FluidStack drained = cursorStorage.drain(toDrain, toFluidAction(action));
            if (action == Action.EXECUTE) {
                screenHandler.setCarried(cursorStorage.getContainer());
            }
            return drained.getAmount();
        });
    }

    private IFluidHandler.FluidAction toFluidAction(Action action) {
        return action == Action.SIMULATE ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
    }

    @Nullable
    private IFluidHandlerItem getFluidCursorStorage() {
        return getFluidStorage(screenHandler.getCarried());
    }

    @Nullable
    private IFluidHandlerItem getFluidStorage(ItemStack stack) {
        return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).orElse(null);
    }

    @Override
    public void onTransfer(int slotIndex) {
        IFluidHandlerItem storage = getFluidStorage(playerInventory.getItem(slotIndex));
        if (storage == null) {
            return;
        }
        FluidStack extractableResource = storage.getFluidInTank(0);
        if (extractableResource == null) {
            return;
        }
        FluidResource fluidResource = ofFluidStack(extractableResource);
        gridService.insert(fluidResource, GridInsertMode.ENTIRE_RESOURCE, (resource, amount, action) -> {
            FluidStack toDrain = toFluidStack(resource, amount == Long.MAX_VALUE ? Integer.MAX_VALUE : amount);
            FluidStack drained = storage.drain(toDrain, toFluidAction(action));
            if (action == Action.EXECUTE) {
                playerInventory.setItem(slotIndex, storage.getContainer());
            }
            return drained.getAmount();
        });
    }

    @Override
    public void onExtract(FluidResource fluidResource, GridExtractMode mode, boolean cursor) {
        throw new UnsupportedOperationException();
    }
}
