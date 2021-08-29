package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.eventhandler;

import com.refinedmods.refinedstorage2.api.grid.eventhandler.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.eventhandler.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.eventhandler.GridScrollMode;
import com.refinedmods.refinedstorage2.api.grid.eventhandler.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.GridExtractPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.GridScrollPacket;
import com.refinedmods.refinedstorage2.platform.fabric.util.ClientPacketUtil;
import com.refinedmods.refinedstorage2.platform.fabric.util.PacketUtil;

public class ClientItemGridEventHandler implements ItemGridEventHandler {
    private boolean active;

    public ClientItemGridEventHandler(boolean active) {
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
