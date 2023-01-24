package com.refinedmods.refinedstorage2.platform.common.packet;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyType;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.GridScrollMode;

import java.util.UUID;

public interface ClientToServerCommunications {
    void sendGridItemExtract(ItemResource itemResource, GridExtractMode mode, boolean cursor);

    void sendGridFluidExtract(FluidResource fluidResource, GridExtractMode mode, boolean cursor);

    void sendGridInsert(GridInsertMode mode, boolean tryAlternatives);

    void sendGridScroll(ItemResource itemResource, GridScrollMode mode, int slotIndex);

    <T> void sendPropertyChange(PropertyType<T> type, T value);

    void sendResourceTypeChange(ResourceType type);

    void sendStorageInfoRequest(UUID storageId);

    void sendResourceFilterSlotAmountChange(int slotIndex, long amount);
}
