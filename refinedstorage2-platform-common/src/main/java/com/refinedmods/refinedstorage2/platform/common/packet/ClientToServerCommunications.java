package com.refinedmods.refinedstorage2.platform.common.packet;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyType;

import java.util.UUID;

public interface ClientToServerCommunications {
    <T> void sendGridExtract(PlatformStorageChannelType<T> storageChannelType,
                             T resource,
                             GridExtractMode mode,
                             boolean cursor);

    <T> void sendGridScroll(PlatformStorageChannelType<T> storageChannelType,
                            T resource,
                            GridScrollMode mode,
                            int slotIndex);

    void sendGridInsert(GridInsertMode mode, boolean tryAlternatives);

    <T> void sendPropertyChange(PropertyType<T> type, T value);

    void sendResourceTypeChange(ResourceType type);

    void sendStorageInfoRequest(UUID storageId);

    void sendResourceFilterSlotAmountChange(int slotIndex, long amount);
}
