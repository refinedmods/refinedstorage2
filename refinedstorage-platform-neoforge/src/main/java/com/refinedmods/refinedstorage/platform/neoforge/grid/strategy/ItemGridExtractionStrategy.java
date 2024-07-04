package com.refinedmods.refinedstorage.platform.neoforge.grid.strategy;

import com.refinedmods.refinedstorage.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage.platform.api.exporter.AmountOverride;
import com.refinedmods.refinedstorage.platform.api.grid.Grid;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.platform.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceTypes;
import com.refinedmods.refinedstorage.platform.neoforge.storage.CapabilityCache;
import com.refinedmods.refinedstorage.platform.neoforge.storage.ItemHandlerInsertableStorage;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;

public class ItemGridExtractionStrategy implements GridExtractionStrategy {
    private final GridOperations gridOperations;
    private final PlayerMainInvWrapper playerInventoryStorage;
    private final CursorItemHandler playerCursorItemHandler;

    public ItemGridExtractionStrategy(final AbstractContainerMenu containerMenu,
                                      final ServerPlayer player,
                                      final Grid grid) {
        this.gridOperations = grid.createOperations(ResourceTypes.ITEM, player);
        this.playerInventoryStorage = new PlayerMainInvWrapper(player.getInventory());
        this.playerCursorItemHandler = new CursorItemHandler(containerMenu);
    }

    @Override
    public boolean onExtract(final PlatformResourceKey resource,
                             final GridExtractMode extractMode,
                             final boolean cursor) {
        if (resource instanceof ItemResource itemResource) {
            final IItemHandler handler = cursor ? playerCursorItemHandler : playerInventoryStorage;
            gridOperations.extract(
                itemResource,
                extractMode,
                new ItemHandlerInsertableStorage(CapabilityCache.ofItemHandler(handler), AmountOverride.NONE)
            );
            return true;
        }
        return false;
    }
}
