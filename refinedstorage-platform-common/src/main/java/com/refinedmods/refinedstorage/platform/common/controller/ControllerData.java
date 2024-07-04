package com.refinedmods.refinedstorage.platform.common.controller;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ControllerData(long stored, long capacity) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ControllerData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_LONG, ControllerData::stored,
        ByteBufCodecs.VAR_LONG, ControllerData::capacity,
        ControllerData::new
    );
}
