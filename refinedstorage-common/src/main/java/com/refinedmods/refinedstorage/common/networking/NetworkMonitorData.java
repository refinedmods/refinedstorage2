package com.refinedmods.refinedstorage.common.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record NetworkMonitorData() {
    public static final NetworkMonitorData INSTANCE = new NetworkMonitorData();
    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkMonitorData> STREAM_CODEC =
        StreamCodec.unit(INSTANCE);
}
