package com.refinedmods.refinedstorage2.fabric.screen.handler.grid;

import com.refinedmods.refinedstorage2.core.grid.*;
import com.refinedmods.refinedstorage2.core.list.StackListListener;
import com.refinedmods.refinedstorage2.core.list.StackListResult;
import com.refinedmods.refinedstorage2.core.storage.StorageChannel;
import com.refinedmods.refinedstorage2.core.util.Action;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.grid.GridBlockEntity;
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
    private final GridView view = new GridView(new FabricGridStackFactory());

    private StorageChannel<ItemStack> storageChannel; // TODO - Support changing of the channel.
    private GridEventHandler eventHandler;

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
        this.eventHandler = new GridEventHandlerImpl(storageChannel, new PlayerGridInteractor(playerInventory.player));

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
    public void onInsertFromCursor(GridInsertMode mode) {
        eventHandler.onInsertFromCursor(mode);
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
                slot.setStack(storageChannel.insert(slot.getStack(), slot.getStack().getCount(), Action.EXECUTE).orElse(ItemStack.EMPTY));
                sendContentUpdates();
            }
        }
        return ItemStack.EMPTY;
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
