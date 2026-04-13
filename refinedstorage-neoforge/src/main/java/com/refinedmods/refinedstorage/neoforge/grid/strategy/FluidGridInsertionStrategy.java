package com.refinedmods.refinedstorage.neoforge.grid.strategy;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.node.grid.GridInsertMode;
import com.refinedmods.refinedstorage.api.network.node.grid.GridOperations;
import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridInsertionStrategy;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.support.resource.ResourceTypes;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import static com.refinedmods.refinedstorage.neoforge.support.resource.VariantUtil.ofPlatform;
import static com.refinedmods.refinedstorage.neoforge.support.resource.VariantUtil.toPlatform;

public class FluidGridInsertionStrategy implements GridInsertionStrategy {
    private final AbstractContainerMenu menu;
    private final ServerPlayer player;
    private final GridOperations gridOperations;

    public FluidGridInsertionStrategy(final AbstractContainerMenu menu, final ServerPlayer player, final Grid grid) {
        this.menu = menu;
        this.player = player;
        this.gridOperations = grid.createOperations(ResourceTypes.FLUID, player);
    }

    @Override
    public boolean onInsert(final GridInsertMode insertMode, final boolean tryAlternatives) {
        final ItemAccess itemAccess = ItemAccess.forPlayerCursor(player, menu);
        final ResourceHandler<net.neoforged.neoforge.transfer.fluid.FluidResource> playerCursor =
            menu.getCarried().getCapability(Capabilities.Fluid.ITEM, itemAccess);
        if (playerCursor == null) {
            return false;
        }
        final net.neoforged.neoforge.transfer.fluid.FluidResource extractableFluid =
            ResourceHandlerUtil.findExtractableResource(playerCursor, r -> true, null);
        if (extractableFluid == null) {
            return false;
        }
        final FluidResource fluidResource = ofPlatform(extractableFluid);
        gridOperations.insert(fluidResource, insertMode, (resource, amount, action, source) -> {
            if (!(resource instanceof FluidResource fluidResource2)) {
                return 0;
            }
            final net.neoforged.neoforge.transfer.fluid.FluidResource platformFluid = toPlatform(fluidResource2);
            try (Transaction tx = Transaction.openRoot()) {
                final long extracted = playerCursor.extract(platformFluid, (int) amount, tx);
                if (action == Action.EXECUTE) {
                    tx.commit();
                }
                return extracted;
            }
        });
        return true;
    }

    @Override
    public boolean onTransfer(final int slotIndex) {
        throw new UnsupportedOperationException();
    }
}
