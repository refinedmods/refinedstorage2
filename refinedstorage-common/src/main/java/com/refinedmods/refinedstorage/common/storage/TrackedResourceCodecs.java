package com.refinedmods.refinedstorage.common.storage;

import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;

import java.util.Optional;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class TrackedResourceCodecs {
    private static final StreamCodec<RegistryFriendlyByteBuf, TrackedResource> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, TrackedResource::getSourceName,
            ByteBufCodecs.VAR_LONG, TrackedResource::getTime,
            TrackedResource::new
        );

    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<TrackedResource>>
        OPTIONAL_STREAM_CODEC = ByteBufCodecs.optional(STREAM_CODEC);

    private TrackedResourceCodecs() {
    }
}
