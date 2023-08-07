package com.refinedmods.refinedstorage2.platform.common.packet;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceInstance;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;

import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;

public interface ServerToClientCommunications {
    void sendControllerEnergyInfo(ServerPlayer player, long stored, long capacity);

    void sendGridActiveness(ServerPlayer player, boolean active);

    <T> void sendGridUpdate(ServerPlayer player,
                            PlatformStorageChannelType<T> storageChannelType,
                            T resource,
                            long change,
                            @Nullable TrackedResource trackedResource);

    void sendGridClear(ServerPlayer player);

    <T> void sendResourceSlotUpdate(ServerPlayer player,
                                    @Nullable ResourceInstance<T> resourceInstance,
                                    int slotIndex);

    void sendStorageInfoResponse(ServerPlayer player, UUID id, StorageInfo storageInfo);
}
