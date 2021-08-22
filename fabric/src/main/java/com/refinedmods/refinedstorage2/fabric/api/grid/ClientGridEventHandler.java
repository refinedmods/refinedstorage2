package com.refinedmods.refinedstorage2.fabric.api.grid;

import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.core.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.core.grid.GridExtractMode;
import com.refinedmods.refinedstorage2.core.grid.GridInsertMode;
import com.refinedmods.refinedstorage2.core.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.core.grid.GridView;
import com.refinedmods.refinedstorage2.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.GridExtractPacket;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.GridScrollPacket;
import com.refinedmods.refinedstorage2.fabric.util.ClientPacketUtil;
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
        ClientPacketUtil.sendToServer(PacketIds.GRID_INSERT_FROM_CURSOR, buf -> buf.writeBoolean(mode == GridInsertMode.SINGLE));
    }

    @Override
    public Rs2ItemStack onInsertFromTransfer(Rs2ItemStack slotStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onExtract(Rs2ItemStack stack, GridExtractMode mode) {
        ClientPacketUtil.sendToServer(PacketIds.GRID_EXTRACT, buf -> {
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
        ClientPacketUtil.sendToServer(PacketIds.GRID_SCROLL, buf -> {
            PacketUtil.writeItemStack(buf, template, false);
            GridScrollPacket.writeMode(buf, mode);
            buf.writeInt(slot);
        });
    }
}
