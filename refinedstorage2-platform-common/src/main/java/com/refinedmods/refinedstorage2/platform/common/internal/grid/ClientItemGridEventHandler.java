package com.refinedmods.refinedstorage2.platform.common.internal.grid;

import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.Platform;

public class ClientItemGridEventHandler implements ItemGridEventHandler {
    @Override
    public void onScroll(final ItemResource itemResource, final GridScrollMode mode, final int slotIndex) {
        Platform.INSTANCE.getClientToServerCommunications().sendGridScroll(itemResource, mode, slotIndex);
    }
}
