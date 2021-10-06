package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.item;

import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.util.ClientPacketUtil;

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
}
