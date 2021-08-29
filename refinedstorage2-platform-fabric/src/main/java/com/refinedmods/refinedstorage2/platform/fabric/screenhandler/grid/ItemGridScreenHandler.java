package com.refinedmods.refinedstorage2.platform.fabric.screenhandler.grid;

import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.grid.eventhandler.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.eventhandler.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.eventhandler.GridScrollMode;
import com.refinedmods.refinedstorage2.api.grid.eventhandler.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.api.grid.view.GridViewImpl;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStackIdentifier;
import com.refinedmods.refinedstorage2.api.stack.list.StackListImpl;
import com.refinedmods.refinedstorage2.api.stack.list.StackListResult;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.util.ItemStacks;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.grid.GridBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.eventhandler.ClientItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.eventhandler.ServerItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.FabricGridStackFactory;
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

public class ItemGridScreenHandler extends GridScreenHandler<Rs2ItemStack> implements ItemGridEventHandler, GridWatcher {
    private static final Logger LOGGER = LogManager.getLogger(GridScreenHandler.class);

    private final ItemGridEventHandler eventHandler;

    public ItemGridScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(Rs2Mod.SCREEN_HANDLERS.getGrid(), syncId, playerInventory, buf, createView());
        this.eventHandler = new ClientItemGridEventHandler(view, isActive());
    }

    public ItemGridScreenHandler(int syncId, PlayerInventory playerInventory, GridBlockEntity<Rs2ItemStack> grid) {
        super(Rs2Mod.SCREEN_HANDLERS.getGrid(), syncId, playerInventory, grid, createView());
        this.eventHandler = new ServerItemGridEventHandler(grid.getContainer().getNode().isActive(), storageChannel, (ServerPlayerEntity) playerInventory.player);
        this.grid.addWatcher(this);
    }

    private static GridViewImpl<Rs2ItemStack, Rs2ItemStackIdentifier> createView() {
        return new GridViewImpl<>(new FabricGridStackFactory(), Rs2ItemStackIdentifier::new, StackListImpl.createItemStackList());
    }

    @Override
    protected Rs2ItemStack readStack(PacketByteBuf buf) {
        return PacketUtil.readItemStack(buf, true);
    }

    @Override
    public void close(PlayerEntity playerEntity) {
        super.close(playerEntity);
        if (grid != null) {
            grid.removeWatcher(this);
        }
    }

    @Override
    public void onChanged(StackListResult<Rs2ItemStack> change) {
        LOGGER.info("Received a change of {} for {}", change.change(), change.stack());

        ServerPacketUtil.sendToPlayer((ServerPlayerEntity) playerInventory.player, PacketIds.GRID_ITEM_UPDATE, buf -> {
            PacketUtil.writeItemStack(buf, change.stack(), false);
            buf.writeLong(change.change());

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(change.stack());
            PacketUtil.writeTrackerEntry(buf, entry);
        });
    }

    @Override
    public void onInsertFromCursor(GridInsertMode mode) {
        eventHandler.onInsertFromCursor(mode);
    }

    @Override
    public Rs2ItemStack onInsertFromTransfer(Rs2ItemStack slotStack) {
        return eventHandler.onInsertFromTransfer(slotStack);
    }

    @Override
    public void onExtract(Rs2ItemStack stack, GridExtractMode mode) {
        eventHandler.onExtract(stack, mode);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity playerEntity, int slotIndex) {
        if (!playerEntity.world.isClient()) {
            Slot slot = getSlot(slotIndex);
            if (slot.hasStack()) {
                Rs2ItemStack slotStack = ItemStacks.ofItemStack(slot.getStack());
                ItemStack resultingStack = ItemStacks.toItemStack(eventHandler.onInsertFromTransfer(slotStack));
                slot.setStack(resultingStack);
                sendContentUpdates();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void onItemUpdate(Rs2ItemStack template, long amount, StorageTracker.Entry trackerEntry) {
        eventHandler.onItemUpdate(template, amount, trackerEntry);
    }

    @Override
    public void onActiveChanged(boolean active) {
        setActive(active);
        eventHandler.onActiveChanged(active);
    }

    @Override
    public void onScroll(Rs2ItemStack stack, int slot, GridScrollMode mode) {
        eventHandler.onScroll(stack, slot, mode);
    }
}
