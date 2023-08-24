package com.refinedmods.refinedstorage2.platform.forge.internal.grid;

import com.refinedmods.refinedstorage2.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.grid.GridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.network.node.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.InteractionCoordinates;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.ItemHandlerInsertableStorage;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

public class ItemGridExtractionStrategy implements GridExtractionStrategy {
    private final GridOperations<ItemResource> gridOperations;
    private final PlayerMainInvWrapper playerInventoryStorage;
    private final CursorStorage playerCursorStorage;

    public ItemGridExtractionStrategy(final AbstractContainerMenu containerMenu,
                                      final Player player,
                                      final Grid grid) {
        this.gridOperations = grid.createOperations(StorageChannelTypes.ITEM, new PlayerActor(player));
        this.playerInventoryStorage = new PlayerMainInvWrapper(player.getInventory());
        this.playerCursorStorage = new CursorStorage(containerMenu);
    }

    @Override
    public <T> boolean onExtract(final PlatformStorageChannelType<T> storageChannelType,
                                 final T resource,
                                 final GridExtractMode extractMode,
                                 final boolean cursor) {
        if (resource instanceof ItemResource itemResource) {
            final IItemHandler handler = cursor ? playerCursorStorage : playerInventoryStorage;
            gridOperations.extract(
                itemResource,
                extractMode,
                new ItemHandlerInsertableStorage(InteractionCoordinates.ofItemHandler(handler), AmountOverride.NONE)
            );
            return true;
        }
        return false;
    }
}
