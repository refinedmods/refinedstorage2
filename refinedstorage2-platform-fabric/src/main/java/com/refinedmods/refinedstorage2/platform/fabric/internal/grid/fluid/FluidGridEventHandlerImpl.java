package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.fluid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
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
        try (Transaction tx = Transaction.openOuter()) {
            ResourceAmount<FluidVariant> fluidVariantAmount = StorageUtil.findExtractableContent(cursorStorage, tx);
            if (fluidVariantAmount == null) {
                return;
            }
            long toExtract = getToExtract(insertMode, fluidVariantAmount);
            long extracted = cursorStorage.extract(fluidVariantAmount.resource(), toExtract, tx);
            if (extracted == 0) {
                return;
            }
            // For fluid insertions, the entire stack needs to be insertable without remainder.
            // We can't guarantee that every fluid storage accepts every remainder amount (buckets for example need at least 1 bucket).
            boolean success = gridService.insert(new com.refinedmods.refinedstorage2.api.resource.ResourceAmount<>(
                    new FluidResource(fluidVariantAmount.resource().getFluid(), fluidVariantAmount.resource().getNbt()),
                    extracted
            ));
            if (success) {
                tx.commit();
            }
        }
    }

    private long getToExtract(GridInsertMode insertMode, ResourceAmount<FluidVariant> fluidVariantAmount) {
        return switch (insertMode) {
            case ENTIRE_RESOURCE -> fluidVariantAmount.amount();
            case SINGLE_RESOURCE -> Math.min(fluidVariantAmount.amount(), FluidConstants.BUCKET);
        };
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
