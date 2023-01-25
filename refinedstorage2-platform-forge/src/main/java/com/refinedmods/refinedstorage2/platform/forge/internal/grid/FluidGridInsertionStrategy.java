package com.refinedmods.refinedstorage2.platform.forge.internal.grid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.PlatformGridServiceFactory;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;

import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.ofFluidStack;
import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.toFluidAction;
import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.toFluidStack;

public class FluidGridInsertionStrategy implements GridInsertionStrategy {
    private final AbstractContainerMenu menu;
    private final GridService<FluidResource> gridService;

    public FluidGridInsertionStrategy(final AbstractContainerMenu menu,
                                      final Player player,
                                      final PlatformGridServiceFactory serviceFactory) {
        this.menu = menu;
        this.gridService = serviceFactory.createForFluid(new PlayerActor(player));
    }

    @Override
    public boolean onInsert(final GridInsertMode insertMode, final boolean tryAlternatives) {
        final IFluidHandlerItem cursorStorage = getFluidCursorStorage();
        if (cursorStorage == null) {
            return false;
        }
        final FluidStack extractableResource = cursorStorage.getFluidInTank(0);
        if (extractableResource.isEmpty()) {
            return false;
        }
        final FluidResource fluidResource = ofFluidStack(extractableResource);
        gridService.insert(fluidResource, insertMode, (resource, amount, action, source) -> {
            final FluidStack toDrain = toFluidStack(resource, amount == Long.MAX_VALUE ? Integer.MAX_VALUE : amount);
            final FluidStack drained = cursorStorage.drain(toDrain, toFluidAction(action));
            if (action == Action.EXECUTE) {
                menu.setCarried(cursorStorage.getContainer());
            }
            return drained.getAmount();
        });
        return true;
    }

    @Nullable
    private IFluidHandlerItem getFluidCursorStorage() {
        return getFluidStorage(menu.getCarried());
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    private IFluidHandlerItem getFluidStorage(final ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null).orElse(null);
    }

    @Override
    public boolean onTransfer(final int slotIndex) {
        throw new UnsupportedOperationException();
    }
}
