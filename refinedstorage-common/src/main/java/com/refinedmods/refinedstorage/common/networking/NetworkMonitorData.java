package com.refinedmods.refinedstorage.common.networking;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record NetworkMonitorData(boolean active, NetworkMonitorNetworkStatistics networkStatistics,
                                 List<NetworkMonitorDeviceGroup> deviceGroups) {
    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkMonitorData> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BOOL, NetworkMonitorData::active,
            NetworkMonitorNetworkStatistics.STREAM_CODEC, NetworkMonitorData::networkStatistics,
            ByteBufCodecs.collection(ArrayList::new, NetworkMonitorDeviceGroup.STREAM_CODEC),
            NetworkMonitorData::deviceGroups,
            NetworkMonitorData::new
        );
}
