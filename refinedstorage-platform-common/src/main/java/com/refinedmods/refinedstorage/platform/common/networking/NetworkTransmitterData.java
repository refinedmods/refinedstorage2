package com.refinedmods.refinedstorage.platform.common.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record NetworkTransmitterData(boolean error, Component message) {
    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkTransmitterData> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BOOL, NetworkTransmitterData::error,
            ComponentSerialization.STREAM_CODEC, NetworkTransmitterData::message,
            NetworkTransmitterData::new
        );

    static NetworkTransmitterData error(final Component message) {
        return new NetworkTransmitterData(true, message);
    }

    static NetworkTransmitterData message(final Component message) {
        return new NetworkTransmitterData(false, message);
    }
}
