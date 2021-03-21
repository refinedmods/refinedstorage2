package com.refinedmods.refinedstorage2.fabric.screen.handler.grid;

import java.util.Optional;

import com.refinedmods.refinedstorage2.core.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.core.grid.GridEventHandlerImpl;
import com.refinedmods.refinedstorage2.core.grid.GridExtractMode;
import com.refinedmods.refinedstorage2.core.grid.GridInsertMode;
import com.refinedmods.refinedstorage2.core.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.core.grid.GridView;
import com.refinedmods.refinedstorage2.core.grid.GridViewImpl;
import com.refinedmods.refinedstorage2.core.list.StackListListener;
import com.refinedmods.refinedstorage2.core.list.StackListResult;
import com.refinedmods.refinedstorage2.core.list.item.ItemStackList;
import com.refinedmods.refinedstorage2.core.storage.StorageChannel;
import com.refinedmods.refinedstorage2.core.storage.StorageTracker;
import com.refinedmods.refinedstorage2.core.util.ItemStackIdentifier;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.grid.GridBlockEntity;
import com.refinedmods.refinedstorage2.fabric.block.entity.grid.GridSettings;
import com.refinedmods.refinedstorage2.fabric.coreimpl.grid.PlayerGridInteractor;
import com.refinedmods.refinedstorage2.fabric.coreimpl.grid.query.FabricGridStackFactory;
import com.refinedmods.refinedstorage2.fabric.packet.s2c.GridItemUpdatePacket;
import com.refinedmods.refinedstorage2.fabric.screen.handler.BaseScreenHandler;
import com.refinedmods.refinedstorage2.fabric.util.PacketUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GridScreenHandler extends BaseScreenHandler implements GridEventHandler, StackListListener<ItemStack> {
    private static final Logger LOGGER = LogManager.getLogger(GridScreenHandler.class);

    private final PlayerInventory playerInventory;
    private final GridView<ItemStack> itemView = new GridViewImpl<>(new FabricGridStackFactory(), ItemStackIdentifier::new, new ItemStackList());

    private GridBlockEntity grid;

    private StorageChannel<ItemStack> storageChannel; // TODO - Support changing of the channel.
    private GridEventHandler eventHandler;

    public GridScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(RefinedStorage2Mod.SCREEN_HANDLERS.getGrid(), syncId);

        this.playerInventory = playerInventory;

        itemView.setSortingDirection(GridSettings.getSortingDirection(buf.readInt()));
        itemView.setSortingType(GridSettings.getSortingType(buf.readInt()));

        addSlots(0);

        int size = buf.readInt();
        for (int i = 0; i < size; ++i) {
            ItemStack stack = buf.readItemStack();
            stack.setCount(buf.readInt());
            StorageTracker.Entry trackerEntry = PacketUtil.readTrackerEntry(buf);
            itemView.loadStack(stack, stack.getCount(), trackerEntry);
        }
        itemView.sort();
    }

    public GridScreenHandler(int syncId, PlayerInventory playerInventory, GridBlockEntity grid) {
        super(RefinedStorage2Mod.SCREEN_HANDLERS.getGrid(), syncId);

        this.playerInventory = playerInventory;
        this.storageChannel = grid.getNetwork().getItemStorageChannel();
        this.storageChannel.addListener(this);
        this.eventHandler = new GridEventHandlerImpl(storageChannel, new PlayerGridInteractor(playerInventory.player));
        this.grid = grid;

        addSlots(0);
    }

    public GridBlockEntity getGrid() {
        return grid;
    }

    @Override
    public void close(PlayerEntity playerEntity) {
        super.close(playerEntity);

        if (storageChannel != null) {
            storageChannel.removeListener(this);
        }
    }

    public void addSlots(int playerInventoryY) {
        slots.clear();

        addPlayerInventory(playerInventory, 8, playerInventoryY);
    }

    @Override
    public void onInsertFromCursor(GridInsertMode mode) {
        eventHandler.onInsertFromCursor(mode);
    }

    @Override
    public void onInsertFromTransfer(Slot slot) {
        eventHandler.onInsertFromTransfer(slot);
    }

    @Override
    public void onExtract(ItemStack stack, GridExtractMode mode) {
        eventHandler.onExtract(stack, mode);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity playerEntity, int slotIndex) {
        if (!playerEntity.world.isClient()) {
            Slot slot = getSlot(slotIndex);
            if (slot.hasStack()) {
                eventHandler.onInsertFromTransfer(slot);
                sendContentUpdates();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void onItemUpdate(ItemStack template, int amount, StorageTracker.Entry trackerEntry) {
        LOGGER.info("Item {} got updated with {}", template, amount);

        itemView.onChange(template, amount, trackerEntry);
    }

    @Override
    public void onScroll(ItemStack stack, int slot, GridScrollMode mode) {
        eventHandler.onScroll(stack, slot, mode);
    }

    @Override
    public void onChanged(StackListResult<ItemStack> change) {
        LOGGER.info("Received a change of {} for {}", change.getChange(), change.getStack());

        PacketUtil.sendToPlayer(playerInventory.player, GridItemUpdatePacket.ID, buf -> {
            PacketUtil.writeItemStackWithoutCount(buf, change.getStack());
            buf.writeInt(change.getChange());

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(change.getStack());
            PacketUtil.writeTrackerEntry(buf, entry);
        });
    }

    public GridView<ItemStack> getItemView() {
        return itemView;
    }
}
