package com.refinedmods.refinedstorage2.fabric.screen.handler.grid;

import alexiil.mc.lib.attributes.item.compat.FixedInventoryVanillaWrapper;
import com.refinedmods.refinedstorage2.core.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.core.grid.GridExtractOption;
import com.refinedmods.refinedstorage2.core.grid.GridView;
import com.refinedmods.refinedstorage2.core.list.StackListResult;
import com.refinedmods.refinedstorage2.core.storage.StorageChannel;
import com.refinedmods.refinedstorage2.core.storage.StorageChannelListener;
import com.refinedmods.refinedstorage2.core.util.Action;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.grid.GridBlockEntity;
import com.refinedmods.refinedstorage2.fabric.packet.s2c.GridItemUpdatePacket;
import com.refinedmods.refinedstorage2.fabric.screen.handler.BaseScreenHandler;
import com.refinedmods.refinedstorage2.fabric.util.PacketUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Set;

public class GridScreenHandler extends BaseScreenHandler implements GridEventHandler, StorageChannelListener<ItemStack> {
    private static final Logger LOGGER = LogManager.getLogger(GridScreenHandler.class);

    private final PlayerInventory playerInventory;
    private StorageChannel<ItemStack> storageChannel; // TODO support changing of the channel.
    private GridView view = new GridView();

    public GridScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(RefinedStorage2Mod.SCREEN_HANDLERS.getGrid(), syncId);

        this.playerInventory = playerInventory;

        addSlots(0);

        int size = buf.readInt();
        for (int i = 0; i < size; ++i) {
            ItemStack stack = buf.readItemStack();
            stack.setCount(buf.readInt());
            view.loadStack(stack, stack.getCount());
        }
        view.sort();
    }

    public GridScreenHandler(int syncId, PlayerInventory playerInventory, GridBlockEntity grid) {
        super(RefinedStorage2Mod.SCREEN_HANDLERS.getGrid(), syncId);

        this.playerInventory = playerInventory;
        this.storageChannel = grid.getNetwork().getItemStorageChannel();
        this.storageChannel.addListener(this);

        addSlots(0);
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

    // TODO - Move this logic to core & add tests.
    @Override
    public void onInsertFromCursor(boolean single) {
        ItemStack cursorStack = playerInventory.getCursorStack();
        LOGGER.info("Inserting {} from cursor with single {}", cursorStack, single);

        if (cursorStack.isEmpty()) {
            return;
        }

        ItemStack remainder;
        if (single) {
            if (!storageChannel.insert(cursorStack, 1, Action.SIMULATE).isPresent()) {
                storageChannel.insert(cursorStack, 1, Action.EXECUTE);
                cursorStack.decrement(1);
            }
            remainder = cursorStack;
        } else {
            remainder = storageChannel
                .insert(cursorStack, cursorStack.getCount(), Action.EXECUTE)
                .orElse(ItemStack.EMPTY);
        }

        playerInventory.setCursorStack(remainder);
        ((ServerPlayerEntity) playerInventory.player).updateCursorStack();
    }

    @Override
    public void onExtract(ServerPlayerEntity player, ItemStack stack, Set<GridExtractOption> options) {
        int itemSize = storageChannel.get(stack).map(ItemStack::getCount).orElse(0);
        if (itemSize == 0) {
            return;
        }

        ItemStack cursorStack = player.inventory.getCursorStack();
        if (!cursorStack.isEmpty()) {
            return;
        }

        int toExtract = getToExtract(options, itemSize);

        Optional<ItemStack> extracted = storageChannel.extract(stack, toExtract, Action.EXECUTE);
        if (extracted.isPresent()) {
            if (options.contains(GridExtractOption.SHIFT)) {
                ItemStack remainder = new FixedInventoryVanillaWrapper(player.inventory).getInsertable().insert(extracted.get());
                if (!remainder.isEmpty()) {
                    storageChannel.insert(remainder, remainder.getCount(), Action.EXECUTE);
                }
            } else {
                player.inventory.setCursorStack(extracted.get());
                player.updateCursorStack();
            }
        }
    }

    @Override
    public ItemStack transferSlot(PlayerEntity playerEntity, int slotIndex) {
        if (!playerEntity.world.isClient()) {
            Slot slot = getSlot(slotIndex);
            if (slot.hasStack()) {
                slot.setStack(storageChannel.insert(slot.getStack(), slot.getStack().getCount(), Action.EXECUTE).orElse(ItemStack.EMPTY));
                sendContentUpdates();
            }
        }
        return ItemStack.EMPTY;
    }

    private int getToExtract(Set<GridExtractOption> options, int itemSize) {
        // TODO - Handle max stack size here.
        if (options.contains(GridExtractOption.HALF) && itemSize > 1) {
            return itemSize / 2;
        } else if (options.contains(GridExtractOption.SINGLE)) {
            return 1;
        } else {
            return 64;
        }
    }

    @Override
    public void onItemUpdate(ItemStack template, int amount) {
        LOGGER.info("Item {} got updated with {}", template, amount);

        view.onChange(template, amount);
    }

    @Override
    public void onChanged(StackListResult<ItemStack> change) {
        LOGGER.info("Received a change of {} for {}", change.getChange(), change.getStack());

        PacketUtil.sendToPlayer(playerInventory.player, GridItemUpdatePacket.ID, buf -> {
            PacketUtil.writeItemStackWithoutCount(buf, change.getStack());
            buf.writeInt(change.getChange());
        });
    }

    public GridView getView() {
        return view;
    }
}
