package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.eventhandler;

import com.refinedmods.refinedstorage2.api.grid.eventhandler.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.util.ClientPacketUtil;

public class ClientFluidGridEventHandler implements FluidGridEventHandler {
    @Override
    public void onInsertFromCursor() {
        ClientPacketUtil.sendToServer(PacketIds.GRID_INSERT_FROM_CURSOR, buf -> {
            buf.writeBoolean(false);
        });
    }

    @Override
    public long onInsertFromTransfer(Rs2FluidStack stack) {
        return stack.getAmount();
    }

    @Override
    public void onActiveChanged(boolean active) {
    }
}
