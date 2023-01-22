package com.refinedmods.refinedstorage2.platform.common.containermenu.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.grid.view.GridViewBuilder;
import com.refinedmods.refinedstorage2.api.grid.view.GridViewBuilderImpl;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.AbstractGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.ClientItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemGridContainerMenu extends AbstractGridContainerMenu<ItemResource> implements ItemGridEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemGridContainerMenu.class);

    private final ItemGridEventHandler itemGridEventHandler;

    public ItemGridContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getGrid(), syncId, playerInventory, buf, createViewBuilder());
        this.itemGridEventHandler = new ClientItemGridEventHandler();
    }

    public ItemGridContainerMenu(final int syncId,
                                 final Inventory playerInventory,
                                 final AbstractGridBlockEntity<ItemResource> grid) {
        super(Menus.INSTANCE.getGrid(), syncId, playerInventory, grid, createViewBuilder());
        final GridService<ItemResource> gridService = grid.getNode().createService(
            new PlayerActor(playerInventory.player),
            ItemGridContainerMenu::getMaxStackSize,
            1
        );
        this.itemGridEventHandler = Platform.INSTANCE.createItemGridEventHandler(this, gridService, playerInventory);
    }

    @SuppressWarnings("deprecation")
    // Forge wants us to use the ItemStack sensitive version - but no way that we will be creating ItemStacks here.
    private static long getMaxStackSize(final ItemResource itemResource) {
        return itemResource.item().getMaxStackSize();
    }

    private static GridViewBuilder createViewBuilder() {
        return new GridViewBuilderImpl(Platform.INSTANCE.getItemGridResourceFactory());
    }

    @Override
    protected ResourceAmount<ItemResource> readResourceAmount(final FriendlyByteBuf buf) {
        return PacketUtil.readItemResourceAmount(buf);
    }

    @Override
    public void onChanged(final ResourceListOperationResult<ItemResource> change,
                          @Nullable final TrackedResource trackedResource) {
        final ItemResource resource = change.resourceAmount().getResource();

        LOGGER.debug("Received a change of {} for {}", change.change(), resource);

        Platform.INSTANCE.getServerToClientCommunications().sendGridItemUpdate(
            (ServerPlayer) playerInventory.player,
            resource,
            change.change(),
            trackedResource
        );
    }

    @Override
    public ItemStack quickMoveStack(final Player playerEntity, final int slotIndex) {
        if (!playerEntity.level.isClientSide()) {
            final Slot slot = getSlot(slotIndex);
            if (slot.hasItem()) {
                itemGridEventHandler.onTransfer(slot.getContainerSlot());
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void onInsert(final GridInsertMode insertMode) {
        itemGridEventHandler.onInsert(insertMode);
    }

    @Override
    public void onTransfer(final int slotIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onExtract(final ItemResource itemResource, final GridExtractMode mode, final boolean cursor) {
        itemGridEventHandler.onExtract(itemResource, mode, cursor);
    }

    @Override
    public void onScroll(final ItemResource itemResource, final GridScrollMode mode, final int slotIndex) {
        itemGridEventHandler.onScroll(itemResource, mode, slotIndex);
    }
}
