package com.refinedmods.refinedstorage2.platform.fabric.screenhandler.grid;

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
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.util.PacketUtil;
import com.refinedmods.refinedstorage2.platform.fabric.util.ServerPacketUtil;

import java.util.Optional;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ItemGridScreenHandler extends GridScreenHandler<ItemResource> implements ItemGridEventHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    private final GridService<ItemResource> gridService;
    private final ItemGridEventHandler itemGridEventHandler;

    public ItemGridScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(Rs2Mod.SCREEN_HANDLERS.getGrid(), syncId, playerInventory, buf, createView());
        this.gridService = null;
        this.itemGridEventHandler = new ClientItemGridEventHandler();
    }

    public ItemGridScreenHandler(int syncId, PlayerInventory playerInventory, GridBlockEntity<ItemResource> grid) {
        super(Rs2Mod.SCREEN_HANDLERS.getGrid(), syncId, playerInventory, grid, createView());
        this.gridService = new GridServiceImpl<>(storageChannel, new PlayerSource(playerInventory.player), itemResource -> (long) itemResource.getItem().getMaxCount());
        this.grid.addWatcher(this);
        this.itemGridEventHandler = new ItemGridEventHandlerImpl(this, gridService, playerInventory);
    }

    private static GridViewImpl<ItemResource> createView() {
        return new GridViewImpl<>(new ItemGridResourceFactory(), new ResourceListImpl<>());
    }

    @Override
    protected ResourceAmount<ItemResource> readStack(PacketByteBuf buf) {
        return PacketUtil.readItemResourceAmount(buf);
    }

    @Override
    public void close(PlayerEntity playerEntity) {
        super.close(playerEntity);
        if (grid != null) {
            grid.removeWatcher(this);
        }
    }

    @Override
    public void onChanged(ResourceListOperationResult<ItemResource> change) {
        LOGGER.info("Received a change of {} for {}", change.change(), change.resourceAmount().getResource());

        ServerPacketUtil.sendToPlayer((ServerPlayerEntity) playerInventory.player, PacketIds.GRID_ITEM_UPDATE, buf -> {
            PacketUtil.writeItemResource(buf, change.resourceAmount().getResource());
            buf.writeLong(change.change());

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(change.resourceAmount().getResource());
            PacketUtil.writeTrackerEntry(buf, entry);
        });
    }

    @Override
    public ItemStack transferSlot(PlayerEntity playerEntity, int slotIndex) {
        if (!playerEntity.world.isClient()) {
            Slot slot = getSlot(slotIndex);
            if (slot.hasStack()) {
                slot.setStack(itemGridEventHandler.onTransfer(slot.getStack()));
                sendContentUpdates();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void onInsert(GridInsertMode insertMode) {
        itemGridEventHandler.onInsert(insertMode);
    }

    @Override
    public ItemStack onTransfer(ItemStack stack) {
        return itemGridEventHandler.onTransfer(stack);
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
