package com.refinedmods.refinedstorage2.platform.common.internal.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.common.Platform;

public class ClientGridInsertionStrategy implements GridInsertionStrategy {
    @Override
    public boolean onInsert(final GridInsertMode insertMode, final boolean tryAlternatives) {
        Platform.INSTANCE.getClientToServerCommunications().sendGridInsert(insertMode, tryAlternatives);
        return true;
    }

    @Override
    public boolean onTransfer(final int slotIndex) {
        throw new UnsupportedOperationException();
    }
}
