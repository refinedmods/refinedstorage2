package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.fluid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.GridExtractPacket;
import com.refinedmods.refinedstorage2.platform.fabric.util.ClientPacketUtil;
import com.refinedmods.refinedstorage2.platform.fabric.util.PacketUtil;

public class ClientFluidGridEventHandler implements FluidGridEventHandler {
    @Override
    public void onInsert(GridInsertMode insertMode) {
        ClientPacketUtil.sendToServer(PacketIds.GRID_INSERT, buf -> buf.writeBoolean(insertMode == GridInsertMode.SINGLE_RESOURCE));
    }

    @Override
    public void onExtract(FluidResource fluidResource, GridExtractMode mode, boolean cursor) {
        ClientPacketUtil.sendToServer(PacketIds.GRID_EXTRACT, buf -> {
            GridExtractPacket.writeMode(buf, mode);
            buf.writeBoolean(cursor);
            PacketUtil.writeFluidResource(buf, fluidResource);
        });
    }
}
