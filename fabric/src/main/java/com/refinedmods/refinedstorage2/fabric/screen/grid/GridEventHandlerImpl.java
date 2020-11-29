package com.refinedmods.refinedstorage2.fabric.screen.grid;

import com.refinedmods.refinedstorage2.fabric.packet.c2s.GridInsertFromCursorPacket;
import com.refinedmods.refinedstorage2.fabric.util.PacketUtil;

public class GridEventHandlerImpl implements GridEventHandler {
    @Override
    public void onInsertFromCursor(boolean single) {
        PacketUtil.sendToServer(GridInsertFromCursorPacket.ID, buf -> buf.writeBoolean(single));
    }
}
