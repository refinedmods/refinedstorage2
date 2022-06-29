package com.refinedmods.refinedstorage2.platform.common.containermenu.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.grid.service.GridServiceImpl;
import com.refinedmods.refinedstorage2.api.grid.view.GridViewImpl;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerSource;
import com.refinedmods.refinedstorage2.platform.apiimpl.grid.ClientItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.AbstractGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.Objects;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ItemGridContainerMenu extends AbstractGridContainerMenu<ItemResource> implements ItemGridEventHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    private final ItemGridEventHandler itemGridEventHandler;

    public ItemGridContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getGrid(), syncId, playerInventory, buf, createView());
        this.itemGridEventHandler = new ClientItemGridEventHandler();
    }

    public ItemGridContainerMenu(final int syncId,
                                 final Inventory playerInventory,
                                 final AbstractGridBlockEntity<ItemResource> grid) {
        super(Menus.INSTANCE.getGrid(), syncId, playerInventory, grid, createView());
        grid.addWatcher(this);
        final GridService<ItemResource> gridService = new GridServiceImpl<>(
                Objects.requireNonNull(storageChannel),
                new PlayerSource(playerInventory.player),
                itemResource -> (long) itemResource.item().getMaxStackSize(),
                1
        );
        this.itemGridEventHandler = Platform.INSTANCE.createItemGridEventHandler(this, gridService, playerInventory);
    }

    @Override
    public void removed(final Player playerEntity) {
        super.removed(playerEntity);
        if (grid != null) {
            grid.removeWatcher(this);
        }
    }

    private static GridViewImpl<ItemResource> createView() {
        return new GridViewImpl<>(Platform.INSTANCE.getItemGridResourceFactory(), new ResourceListImpl<>());
    }

    @Override
    protected ResourceAmount<ItemResource> readResourceAmount(final FriendlyByteBuf buf) {
        return PacketUtil.readItemResourceAmount(buf);
    }

    @Override
    public void onChanged(final ResourceListOperationResult<ItemResource> change) {
        final ItemResource resource = change.resourceAmount().getResource();

        LOGGER.info("Received a change of {} for {}", change.change(), resource);

        Platform.INSTANCE.getServerToClientCommunications().sendGridItemUpdate(
                (ServerPlayer) playerInventory.player,
                resource,
                change.change(),
                Objects.requireNonNull(storageChannel)
                        .findTrackedResourceBySourceType(resource, PlayerSource.class).orElse(null)
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
