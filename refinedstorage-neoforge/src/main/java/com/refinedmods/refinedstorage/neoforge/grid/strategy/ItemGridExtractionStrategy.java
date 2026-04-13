package com.refinedmods.refinedstorage.neoforge.grid.strategy;

import com.refinedmods.refinedstorage.api.network.node.grid.GridExtractMode;
import com.refinedmods.refinedstorage.api.network.node.grid.GridOperations;
import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.support.resource.ResourceTypes;
import com.refinedmods.refinedstorage.neoforge.storage.ResourceHandlerInsertableStorage;
import com.refinedmods.refinedstorage.neoforge.storage.StaticResourceHandlerProvider;
import com.refinedmods.refinedstorage.neoforge.support.resource.VariantUtil;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.CarriedSlotWrapper;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;

public class ItemGridExtractionStrategy implements GridExtractionStrategy {
    private final GridOperations gridOperations;
    private final ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> playerInventory;
    private final ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> playerCursor;

    public ItemGridExtractionStrategy(final AbstractContainerMenu containerMenu,
                                      final ServerPlayer player,
                                      final Grid grid) {
        this.gridOperations = grid.createOperations(ResourceTypes.ITEM, player);
        this.playerInventory = PlayerInventoryWrapper.of(player);
        this.playerCursor = CarriedSlotWrapper.of(containerMenu);
    }

    @Override
    public boolean onExtract(final PlatformResourceKey resource,
                             final GridExtractMode extractMode,
                             final boolean cursor) {
        if (resource instanceof ItemResource itemResource) {
            final ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> handler = cursor
                ? playerCursor
                : playerInventory;
            gridOperations.extract(
                itemResource,
                extractMode,
                new ResourceHandlerInsertableStorage<>(new StaticResourceHandlerProvider<>(
                    handler,
                    VariantUtil::ofPlatform
                ), VariantUtil::optionalItemToPlatform)
            );
            return true;
        }
        return false;
    }
}
