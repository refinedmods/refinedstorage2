package com.refinedmods.refinedstorage2.platform.forge.grid.strategy;

import com.refinedmods.refinedstorage2.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage2.platform.api.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.forge.storage.CapabilityCache;
import com.refinedmods.refinedstorage2.platform.forge.storage.ItemHandlerInsertableStorage;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;

public class ItemGridExtractionStrategy implements GridExtractionStrategy {
    private final GridOperations<ItemResource> gridOperations;
    private final PlayerMainInvWrapper playerInventoryStorage;
    private final CursorItemHandler playerCursorItemHandler;

    public ItemGridExtractionStrategy(final AbstractContainerMenu containerMenu,
                                      final Player player,
                                      final Grid grid) {
        this.gridOperations = grid.createOperations(StorageChannelTypes.ITEM, new PlayerActor(player));
        this.playerInventoryStorage = new PlayerMainInvWrapper(player.getInventory());
        this.playerCursorItemHandler = new CursorItemHandler(containerMenu);
    }

    @Override
    public <T> boolean onExtract(final PlatformStorageChannelType<T> storageChannelType,
                                 final T resource,
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
