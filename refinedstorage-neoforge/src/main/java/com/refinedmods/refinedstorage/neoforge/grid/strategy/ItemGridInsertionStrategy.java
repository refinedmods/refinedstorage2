package com.refinedmods.refinedstorage.neoforge.grid.strategy;

import com.refinedmods.refinedstorage.api.network.node.grid.GridInsertMode;
import com.refinedmods.refinedstorage.api.network.node.grid.GridOperations;
import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridInsertionStrategy;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.support.resource.ResourceTypes;
import com.refinedmods.refinedstorage.neoforge.storage.ResourceHandlerExtractableStorage;
import com.refinedmods.refinedstorage.neoforge.storage.ResourceHandlerProvider;
import com.refinedmods.refinedstorage.neoforge.storage.StaticResourceHandlerProvider;
import com.refinedmods.refinedstorage.neoforge.support.resource.VariantUtil;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.CarriedSlotWrapper;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;

import static com.refinedmods.refinedstorage.neoforge.support.resource.VariantUtil.ofPlatform;

public class ItemGridInsertionStrategy implements GridInsertionStrategy {
    private final AbstractContainerMenu containerMenu;
    private final GridOperations gridOperations;
    private final ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> playerCursor;

    public ItemGridInsertionStrategy(final AbstractContainerMenu containerMenu,
                                     final ServerPlayer player,
                                     final Grid grid) {
        this.containerMenu = containerMenu;
        this.gridOperations = grid.createOperations(ResourceTypes.ITEM, player);
        this.playerCursor = CarriedSlotWrapper.of(containerMenu);
    }

    @Override
    public boolean onInsert(final GridInsertMode insertMode, final boolean tryAlternatives) {
        final ItemStack carried = containerMenu.getCarried();
        if (carried.isEmpty()) {
            return false;
        }
        final ItemResource itemResource = ItemResource.ofItemStack(carried);
        gridOperations.insert(
            itemResource,
            insertMode,
            new ResourceHandlerExtractableStorage<>(new StaticResourceHandlerProvider<>(
                playerCursor,
                VariantUtil::ofPlatform
            ), VariantUtil::optionalItemToPlatform)
        );
        return true;
    }

    @Override
    public boolean onTransfer(final int slotIndex) {
        final Slot slot = containerMenu.getSlot(slotIndex);
        final RangedResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> handler =
            RangedResourceHandler.ofSingleIndex(VanillaContainerWrapper.of(slot.container), slot.getContainerSlot());
        final net.neoforged.neoforge.transfer.item.ItemResource itemInSlot =
            ResourceHandlerUtil.findExtractableResource(handler, r -> true, null);
        if (itemInSlot == null) {
            return false;
        }
        final ResourceHandlerProvider<net.neoforged.neoforge.transfer.item.ItemResource> provider =
            new StaticResourceHandlerProvider<>(handler, VariantUtil::ofPlatform);
        gridOperations.insert(
            ofPlatform(itemInSlot),
            GridInsertMode.ENTIRE_RESOURCE,
            new ResourceHandlerExtractableStorage<>(provider, VariantUtil::optionalItemToPlatform)
        );
        return true;
    }
}
