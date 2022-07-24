package com.refinedmods.refinedstorage2.platform.common.packet;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;

import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;

public interface ServerToClientCommunications {
    void sendControllerEnergyInfo(ServerPlayer player, long stored, long capacity);

    void sendGridActiveness(ServerPlayer player, boolean active);

    void sendGridFluidUpdate(ServerPlayer player,
                             FluidResource fluidResource,
                             long change,
                             @Nullable TrackedResource trackedResource);

    void sendGridItemUpdate(ServerPlayer player,
                            ItemResource itemResource,
                            long change,
                            @Nullable TrackedResource trackedResource);

    void sendResourceFilterSlotUpdate(ServerPlayer player,
                                      ResourceFilterContainer resourceFilterContainer,
                                      int slotIndex,
                                      int containerIndex);

    void sendStorageInfoResponse(ServerPlayer player, UUID id, StorageInfo storageInfo);
}
