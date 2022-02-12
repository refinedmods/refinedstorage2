package com.refinedmods.refinedstorage2.platform.abstractions.packet;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;

import java.util.UUID;

public interface ClientToServerCommunications {
    void sendGridItemExtract(ItemResource itemResource, GridExtractMode mode, boolean cursor);

    void sendGridFluidExtract(FluidResource fluidResource, GridExtractMode mode, boolean cursor);

    void sendGridInsert(GridInsertMode mode);

    void sendGridScroll(ItemResource itemResource, GridScrollMode mode, int slot);

    void sendPropertyChange(int id, int value);

    void sendResourceTypeChange(ResourceType<?> type);

    void sendStorageInfoRequest(UUID storageId);
}
