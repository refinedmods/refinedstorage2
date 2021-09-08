package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.eventhandler;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.eventhandler.FluidGridInteractor;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerFluidGridInteractor implements FluidGridInteractor {
    private final PlayerEntity player;

    public PlayerFluidGridInteractor(PlayerEntity player) {
        this.player = player;
    }

    private Storage<FluidVariant> getCursorStorage() {
        return ContainerItemContext.ofPlayerCursor(player, player.currentScreenHandler).find(FluidStorage.ITEM);
    }

    @Override
    public Rs2FluidStack getCursorStack() {
        ResourceAmount<FluidVariant> resourceAmount = StorageUtil.findExtractableContent(getCursorStorage(), null);
        if (resourceAmount == null) {
            return Rs2FluidStack.EMPTY;
        }
        return new Rs2FluidStack(Rs2PlatformApiFacade.INSTANCE.toRs2Fluid(resourceAmount.resource()), resourceAmount.amount(), resourceAmount.resource().getNbt());
    }

    @Override
    public Rs2FluidStack extractAllFromCursor(Action action) {
        return extractFromCursor(action, getCursorStack().getAmount());
    }

    @Override
    public Rs2FluidStack extractFromCursor(Action action, long amount) {
        try (Transaction trans = Transaction.openOuter()) {
            Storage<FluidVariant> cursorStorage = getCursorStorage();
            FluidVariant resource = StorageUtil.findExtractableResource(cursorStorage, trans);
            if (resource == null) {
                return Rs2FluidStack.EMPTY;
            }

            long amountExtracted = cursorStorage.extract(resource, amount, trans);
            if (amountExtracted == 0) {
                return Rs2FluidStack.EMPTY;
            }

            if (action == Action.EXECUTE) {
                trans.commit();
            }

            return new Rs2FluidStack(
                    Rs2PlatformApiFacade.INSTANCE.toRs2Fluid(resource),
                    amountExtracted,
                    resource.getNbt()
            );
        }
    }

    @Override
    public String getName() {
        return player.getGameProfile().getName();
    }
}
