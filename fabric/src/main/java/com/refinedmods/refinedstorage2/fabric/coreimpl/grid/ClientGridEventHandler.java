package com.refinedmods.refinedstorage2.fabric.coreimpl.grid;

import com.refinedmods.refinedstorage2.core.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.core.grid.GridExtractMode;
import com.refinedmods.refinedstorage2.core.grid.GridInsertMode;
import com.refinedmods.refinedstorage2.core.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.core.grid.GridView;
import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.storage.StorageTracker;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.GridExtractPacket;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.GridInsertFromCursorPacket;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.GridScrollPacket;
import com.refinedmods.refinedstorage2.fabric.util.PacketUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientGridEventHandler implements GridEventHandler {
    private static final Logger LOGGER = LogManager.getLogger(ClientGridEventHandler.class);

    private boolean active;
    private final GridView<Rs2ItemStack> itemView;

    public ClientGridEventHandler(boolean active, GridView<Rs2ItemStack> itemView) {
        this.itemView = itemView;
        this.active = active;
    }

    @Override
    public void onInsertFromCursor(GridInsertMode mode) {
        PacketUtil.sendToServer(GridInsertFromCursorPacket.ID, buf -> buf.writeBoolean(mode == GridInsertMode.SINGLE));
    }

    @Override
    public Rs2ItemStack onInsertFromTransfer(Rs2ItemStack slotStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onExtract(Rs2ItemStack stack, GridExtractMode mode) {
        PacketUtil.sendToServer(GridExtractPacket.ID, buf -> {
            PacketUtil.writeItemStack(buf, stack, false);
            GridExtractPacket.writeMode(buf, mode);
        });
    }

    @Override
    public void onItemUpdate(Rs2ItemStack template, long amount, StorageTracker.Entry trackerEntry) {
        LOGGER.info("Item {} got updated with {}", template, amount);
        itemView.onChange(template, amount, trackerEntry);
    }

    @Override
    public void onActiveChanged(boolean active) {
        this.active = active;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void onScroll(Rs2ItemStack template, int slot, GridScrollMode mode) {
        PacketUtil.sendToServer(GridScrollPacket.ID, buf -> {
            PacketUtil.writeItemStack(buf, template, false);
            GridScrollPacket.writeMode(buf, mode);
            buf.writeInt(slot);
        });
    }
}
