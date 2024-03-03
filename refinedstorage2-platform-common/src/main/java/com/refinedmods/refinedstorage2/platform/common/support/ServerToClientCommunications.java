package com.refinedmods.refinedstorage2.platform.common.support;

import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.common.networking.NetworkTransmitterStatus;

import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;

public interface ServerToClientCommunications {
    void sendEnergyInfo(ServerPlayer player, long stored, long capacity);

    void sendWirelessTransmitterRange(ServerPlayer player, int range);

    void sendGridActiveness(ServerPlayer player, boolean active);

    void sendGridUpdate(ServerPlayer player,
                        PlatformStorageChannelType storageChannelType,
                        ResourceKey resource,
                        long change,
                        @Nullable TrackedResource trackedResource);

    void sendGridClear(ServerPlayer player);

    void sendResourceSlotUpdate(ServerPlayer player,
                                @Nullable ResourceAmountTemplate resourceAmount,
                                int slotIndex);

    void sendStorageInfoResponse(ServerPlayer player, UUID id, StorageInfo storageInfo);

    void sendNetworkTransmitterStatus(ServerPlayer player, NetworkTransmitterStatus status);
}
