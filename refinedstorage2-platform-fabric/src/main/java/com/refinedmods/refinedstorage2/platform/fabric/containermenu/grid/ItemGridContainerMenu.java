package com.refinedmods.refinedstorage2.platform.fabric.containermenu.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.grid.service.GridServiceImpl;
import com.refinedmods.refinedstorage2.api.grid.view.GridViewImpl;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.grid.GridBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.item.ClientItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.item.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.item.ItemGridEventHandlerImpl;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.ItemGridResourceFactory;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.PlayerSource;
import com.refinedmods.refinedstorage2.platform.fabric.mixin.SlotAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.util.PacketUtil;
import com.refinedmods.refinedstorage2.platform.fabric.util.ServerPacketUtil;

import java.util.Optional;

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
        super(Rs2Mod.MENUS.getGrid(), syncId, playerInventory, buf, createView());
        this.gridService = null;
        this.itemGridEventHandler = new ClientItemGridEventHandler();
    }

    public ItemGridContainerMenu(int syncId, Inventory playerInventory, GridBlockEntity<ItemResource> grid) {
        super(Rs2Mod.MENUS.getGrid(), syncId, playerInventory, grid, createView());
        this.gridService = new GridServiceImpl<>(storageChannel, new PlayerSource(playerInventory.player), itemResource -> (long) itemResource.getItem().getMaxStackSize());
        this.grid.addWatcher(this);
        this.itemGridEventHandler = new ItemGridEventHandlerImpl(this, gridService, playerInventory);
    }

    private static GridViewImpl<ItemResource> createView() {
        return new GridViewImpl<>(new ItemGridResourceFactory(), new ResourceListImpl<>());
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
        LOGGER.info("Received a change of {} for {}", change.change(), change.resourceAmount().getResource());

        ServerPacketUtil.sendToPlayer((ServerPlayer) playerInventory.player, PacketIds.GRID_ITEM_UPDATE, buf -> {
            PacketUtil.writeItemResource(buf, change.resourceAmount().getResource());
            buf.writeLong(change.change());

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(change.resourceAmount().getResource());
            PacketUtil.writeTrackerEntry(buf, entry);
        });
    }

    @Override
    public ItemStack quickMoveStack(Player playerEntity, int slotIndex) {
        if (!playerEntity.level.isClientSide()) {
            Slot slot = getSlot(slotIndex);
            if (slot.hasItem()) {
                itemGridEventHandler.onTransfer(((SlotAccessor) slot).getSlot());
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
