package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.item;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.fabric.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.GridExtractPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.GridScrollPacket;
import com.refinedmods.refinedstorage2.platform.fabric.util.ClientPacketUtil;
import com.refinedmods.refinedstorage2.platform.fabric.util.PacketUtil;

import net.minecraft.item.ItemStack;

public class ClientItemGridEventHandler implements ItemGridEventHandler {
    @Override
    public void onInsert(GridInsertMode insertMode) {
        ClientPacketUtil.sendToServer(PacketIds.GRID_INSERT, buf -> buf.writeBoolean(insertMode == GridInsertMode.SINGLE_RESOURCE));
    }

    @Override
    public ItemStack onTransfer(ItemStack stack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onExtract(ItemResource itemResource, GridExtractMode mode, boolean cursor) {
        ClientPacketUtil.sendToServer(PacketIds.GRID_EXTRACT, buf -> {
            GridExtractPacket.writeMode(buf, mode);
            buf.writeBoolean(cursor);
            PacketUtil.writeItemResource(buf, itemResource);
        });
    }

    @Override
    public void onScroll(ItemResource itemResource, GridScrollMode mode, int slot) {
        ClientPacketUtil.sendToServer(PacketIds.GRID_SCROLL, buf -> {
            PacketUtil.writeItemResource(buf, itemResource);
            GridScrollPacket.writeMode(buf, mode);
            buf.writeInt(slot);
        });
    }
}
