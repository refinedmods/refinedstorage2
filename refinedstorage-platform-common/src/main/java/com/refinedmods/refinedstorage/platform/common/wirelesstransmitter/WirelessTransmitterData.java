package com.refinedmods.refinedstorage.platform.common.wirelesstransmitter;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record WirelessTransmitterData(int range) {
    public static final StreamCodec<RegistryFriendlyByteBuf, WirelessTransmitterData> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, WirelessTransmitterData::range,
            WirelessTransmitterData::new
        );
}
