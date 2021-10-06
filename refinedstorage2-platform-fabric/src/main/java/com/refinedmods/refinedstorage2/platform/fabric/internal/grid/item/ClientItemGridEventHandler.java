package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.item;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.GridExtractPacket;
import com.refinedmods.refinedstorage2.platform.fabric.util.ClientPacketUtil;
import com.refinedmods.refinedstorage2.platform.fabric.util.PacketUtil;

import net.minecraft.item.ItemStack;

public class ClientItemGridEventHandler implements ItemGridEventHandler {
    @Override
    public void insert(GridInsertMode insertMode) {
        ClientPacketUtil.sendToServer(PacketIds.GRID_INSERT, buf -> buf.writeBoolean(insertMode == GridInsertMode.SINGLE_RESOURCE));
    }

    @Override
    public ItemStack transfer(ItemStack stack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onExtract(ItemResource itemResource, GridExtractMode mode, boolean cursor) {
        ClientPacketUtil.sendToServer(PacketIds.GRID_EXTRACT, buf -> {
            PacketUtil.writeItemResource(buf, itemResource);
            GridExtractPacket.writeMode(buf, mode);
            buf.writeBoolean(cursor);
        });
    }
}
