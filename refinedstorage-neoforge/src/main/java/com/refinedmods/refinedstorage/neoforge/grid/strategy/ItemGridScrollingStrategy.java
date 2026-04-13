package com.refinedmods.refinedstorage.neoforge.grid.strategy;

import com.refinedmods.refinedstorage.api.network.node.grid.GridExtractMode;
import com.refinedmods.refinedstorage.api.network.node.grid.GridInsertMode;
import com.refinedmods.refinedstorage.api.network.node.grid.GridOperations;
import com.refinedmods.refinedstorage.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.support.resource.ResourceTypes;
import com.refinedmods.refinedstorage.neoforge.storage.ResourceHandlerExtractableStorage;
import com.refinedmods.refinedstorage.neoforge.storage.ResourceHandlerInsertableStorage;
import com.refinedmods.refinedstorage.neoforge.storage.StaticResourceHandlerProvider;
import com.refinedmods.refinedstorage.neoforge.support.resource.VariantUtil;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.CarriedSlotWrapper;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;

public class ItemGridScrollingStrategy implements GridScrollingStrategy {
    private final GridOperations gridOperations;
    private final ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> playerInventory;
    private final ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> playerCursor;

    public ItemGridScrollingStrategy(final AbstractContainerMenu containerMenu,
                                     final ServerPlayer player,
                                     final Grid grid) {
        this.gridOperations = grid.createOperations(ResourceTypes.ITEM, player);
        this.playerInventory = PlayerInventoryWrapper.of(player);
        this.playerCursor = CarriedSlotWrapper.of(containerMenu);
    }

    @Override
    public boolean onScroll(final PlatformResourceKey resource, final GridScrollMode scrollMode, final int slotIndex) {
        if (resource instanceof ItemResource itemResource) {
            final ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> playerStorage = slotIndex >= 0
                ? RangedResourceHandler.ofSingleIndex(playerInventory, slotIndex)
                : playerInventory;
            switch (scrollMode) {
                case GRID_TO_INVENTORY -> handleGridToInventoryScroll(itemResource, playerStorage);
                case INVENTORY_TO_GRID -> handleInventoryToGridScroll(itemResource, playerStorage);
                case GRID_TO_CURSOR -> handleGridToInventoryScroll(itemResource, playerCursor);
            }
            return true;
        }
        return false;
    }

    private void handleInventoryToGridScroll(
        final ItemResource itemResource,
        final ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> handler
    ) {
        final ExtractableStorage extractableStorage = new ResourceHandlerExtractableStorage<>(
            new StaticResourceHandlerProvider<>(handler, VariantUtil::ofPlatform),
            VariantUtil::optionalItemToPlatform
        );
        gridOperations.insert(itemResource, GridInsertMode.SINGLE_RESOURCE, extractableStorage);
    }

    private void handleGridToInventoryScroll(
        final ItemResource itemResource,
        final ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> handler
    ) {
        final InsertableStorage insertableStorage = new ResourceHandlerInsertableStorage<>(
            new StaticResourceHandlerProvider<>(handler, VariantUtil::ofPlatform),
            VariantUtil::optionalItemToPlatform
        );
        gridOperations.extract(itemResource, GridExtractMode.SINGLE_RESOURCE, insertableStorage);
    }
}
