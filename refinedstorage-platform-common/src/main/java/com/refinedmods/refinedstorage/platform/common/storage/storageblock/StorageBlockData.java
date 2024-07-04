package com.refinedmods.refinedstorage.platform.common.storage.storageblock;

import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceContainerData;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record StorageBlockData(long stored, long capacity, ResourceContainerData resourceContainerData) {
    public static final StreamCodec<RegistryFriendlyByteBuf, StorageBlockData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_LONG, StorageBlockData::stored,
        ByteBufCodecs.VAR_LONG, StorageBlockData::capacity,
        ResourceContainerData.STREAM_CODEC, StorageBlockData::resourceContainerData,
        StorageBlockData::new
    );
}
