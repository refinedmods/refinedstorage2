package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.fluid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import org.jetbrains.annotations.Nullable;

public class FluidGridEventHandlerImpl implements FluidGridEventHandler {
    private final ScreenHandler screenHandler;
    private final PlayerEntity player;
    private final GridService<FluidResource> gridService;
    private final PlayerInventoryStorage playerInventoryStorage;

    public FluidGridEventHandlerImpl(ScreenHandler screenHandler, GridService<FluidResource> gridService, PlayerInventory playerInventory) {
        this.screenHandler = screenHandler;
        this.player = playerInventory.player;
        this.gridService = gridService;
        this.playerInventoryStorage = PlayerInventoryStorage.of(playerInventory);
    }

    @Override
    public void onInsert(GridInsertMode insertMode) {
        Storage<FluidVariant> cursorStorage = getCursorStorage();
        if (cursorStorage == null) {
            return;
        }
        FluidVariant extractableResource = StorageUtil.findExtractableResource(cursorStorage, null);
        if (extractableResource == null) {
            return;
        }
        FluidResource fluidResource = new FluidResource(extractableResource.getFluid(), extractableResource.getNbt());
        gridService.insert(fluidResource, insertMode, (resource, amount, action) -> {
            FluidVariant fluidVariant = resource.getFluidVariant();
            try (Transaction tx = Transaction.openOuter()) {
                long extracted = cursorStorage.extract(fluidVariant, amount, tx);
                if (action == Action.EXECUTE) {
                    tx.commit();
                }
                return extracted;
            }
        });
    }

    @Nullable
    private Storage<FluidVariant> getCursorStorage() {
        return FluidStorage.ITEM.find(
                screenHandler.getCursorStack(),
                ContainerItemContext.ofPlayerCursor(player, screenHandler)
        );
    }

    @Override
    public void onExtract(FluidResource fluidResource, GridExtractMode mode, boolean cursor) {
    }
}
