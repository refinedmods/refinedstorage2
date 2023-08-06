package com.refinedmods.refinedstorage2.platform.forge.internal.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.platform.api.grid.GridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.PlatformGridServiceFactory;
import com.refinedmods.refinedstorage2.platform.api.network.node.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.InteractionCoordinates;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.ItemHandlerInsertableStorage;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

public class ItemGridExtractionStrategy implements GridExtractionStrategy {
    private final GridService<ItemResource> gridService;
    private final PlayerMainInvWrapper playerInventoryStorage;
    private final CursorStorage playerCursorStorage;

    public ItemGridExtractionStrategy(final AbstractContainerMenu containerMenu,
                                      final Player player,
                                      final PlatformGridServiceFactory gridServiceFactory) {
        this.gridService = gridServiceFactory.createForItem(new PlayerActor(player));
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
            gridService.extract(
                itemResource,
                extractMode,
                new ItemHandlerInsertableStorage(InteractionCoordinates.ofItemHandler(handler), AmountOverride.NONE)
            );
            return true;
        }
        return false;
    }
}
