package com.refinedmods.refinedstorage2.platform.common.containermenu.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.grid.service.GridServiceImpl;
import com.refinedmods.refinedstorage2.api.grid.view.GridViewImpl;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;
import com.refinedmods.refinedstorage2.platform.abstractions.Platform;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.GridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.ClientItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.PlayerSource;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ItemGridContainerMenu extends GridContainerMenu<ItemResource> implements ItemGridEventHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    private final GridService<ItemResource> gridService;
    private final ItemGridEventHandler itemGridEventHandler;

    public ItemGridContainerMenu(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getGrid(), syncId, playerInventory, buf, createView());
        this.gridService = null;
        this.itemGridEventHandler = new ClientItemGridEventHandler();
    }

    public ItemGridContainerMenu(int syncId, Inventory playerInventory, GridBlockEntity<ItemResource> grid) {
        super(Menus.INSTANCE.getGrid(), syncId, playerInventory, grid, createView());
        this.gridService = new GridServiceImpl<>(storageChannel, new PlayerSource(playerInventory.player), itemResource -> (long) itemResource.getItem().getMaxStackSize(), 1);
        this.grid.addWatcher(this);
        this.itemGridEventHandler = Platform.INSTANCE.createItemGridEventHandler(this, gridService, playerInventory);
    }

    private static GridViewImpl<ItemResource> createView() {
        return new GridViewImpl<>(Platform.INSTANCE.getItemGridResourceFactory(), new ResourceListImpl<>());
    }

    @Override
    protected ResourceAmount<ItemResource> readResourceAmount(FriendlyByteBuf buf) {
        return PacketUtil.readItemResourceAmount(buf);
    }

    @Override
    public void removed(Player playerEntity) {
        super.removed(playerEntity);
        if (grid != null) {
            grid.removeWatcher(this);
        }
    }

    @Override
    public void onChanged(ResourceListOperationResult<ItemResource> change) {
        ItemResource resource = change.resourceAmount().getResource();

        LOGGER.info("Received a change of {} for {}", change.change(), resource);

        Platform.INSTANCE.getServerToClientCommunications().sendGridItemUpdate(
                (ServerPlayer) playerInventory.player,
                resource,
                change.change(),
                storageChannel.findTrackedResourceBySourceType(resource, PlayerSource.class).orElse(null)
        );
    }

    @Override
    public ItemStack quickMoveStack(Player playerEntity, int slotIndex) {
        if (!playerEntity.level.isClientSide()) {
            Slot slot = getSlot(slotIndex);
            if (slot.hasItem()) {
                itemGridEventHandler.onTransfer(slot.getContainerSlot());
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void onInsert(GridInsertMode insertMode) {
        itemGridEventHandler.onInsert(insertMode);
    }

    @Override
    public void onTransfer(int slotIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onExtract(ItemResource itemResource, GridExtractMode mode, boolean cursor) {
        itemGridEventHandler.onExtract(itemResource, mode, cursor);
    }

    @Override
    public void onScroll(ItemResource itemResource, GridScrollMode mode, int slot) {
        itemGridEventHandler.onScroll(itemResource, mode, slot);
    }
}
