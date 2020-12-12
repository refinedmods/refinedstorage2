package com.refinedmods.refinedstorage2.fabric.screen.handler.grid;

import com.refinedmods.refinedstorage2.core.grid.GridView;
import com.refinedmods.refinedstorage2.core.list.StackListResult;
import com.refinedmods.refinedstorage2.core.storage.StorageChannel;
import com.refinedmods.refinedstorage2.core.storage.StorageChannelListener;
import com.refinedmods.refinedstorage2.core.util.Action;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.grid.GridBlockEntity;
import com.refinedmods.refinedstorage2.fabric.screen.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.fabric.screen.handler.BaseScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;

public class GridScreenHandler extends BaseScreenHandler implements GridEventHandler, StorageChannelListener<ItemStack> {
    private final PlayerInventory playerInventory;
    private StorageChannel<ItemStack> storageChannel; // TODO support changing of the channel.
    private GridView view = new GridView();

    public GridScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(RefinedStorage2Mod.SCREEN_HANDLERS.getGrid(), syncId);

        this.playerInventory = playerInventory;

        addSlots(0);

        int size = buf.readInt();
        for (int i = 0; i< size;++i) {
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

    @Override
    public void onInsertFromCursor(boolean single) {
        System.out.println("Inserting " + playerInventory.getCursorStack() + " (single=" + single + ")");

        ItemStack remainder = storageChannel
            .insert(playerInventory.getCursorStack(), single ? 1 : playerInventory.getCursorStack().getCount(), Action.EXECUTE)
            .orElse(ItemStack.EMPTY);

        playerInventory.setCursorStack(remainder);
    }

    @Override
    public void onChanged(StackListResult<ItemStack> change) {
        System.out.println("Received a change... " + change.getChange() + " " + change.getStack());
    }

    public GridView getView() {
        return view;
    }
}
