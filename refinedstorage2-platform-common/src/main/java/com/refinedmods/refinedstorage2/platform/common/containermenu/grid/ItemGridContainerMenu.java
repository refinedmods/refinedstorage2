package com.refinedmods.refinedstorage2.platform.common.containermenu.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.AbstractGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.ClientItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class ItemGridContainerMenu extends AbstractGridContainerMenu<ItemResource> implements ItemGridEventHandler {
    private final ItemGridEventHandler itemGridEventHandler;

    public ItemGridContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getGrid(), syncId, playerInventory, buf);
        this.itemGridEventHandler = new ClientItemGridEventHandler();
    }

    public ItemGridContainerMenu(final int syncId,
                                 final Inventory playerInventory,
                                 final AbstractGridBlockEntity<ItemResource> grid) {
        super(Menus.INSTANCE.getGrid(), syncId, playerInventory, grid);
        final GridService<ItemResource> gridService = grid.getNode().create(
            StorageChannelTypes.ITEM,
            new PlayerActor(playerInventory.player),
            ItemGridContainerMenu::getMaxStackSize,
            1
        );
        this.itemGridEventHandler = Platform.INSTANCE.createItemGridEventHandler(this, gridService, playerInventory);
    }

    // TODO - Remove!
    private static long getMaxStackSize(final ItemResource itemResource) {
        return itemResource.item().getMaxStackSize();
    }

    @Override
    public void onScroll(final ItemResource itemResource, final GridScrollMode mode, final int slotIndex) {
        itemGridEventHandler.onScroll(itemResource, mode, slotIndex);
    }
}
