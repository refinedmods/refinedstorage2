package com.refinedmods.refinedstorage.common.iface;

import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record InterfaceData(ResourceContainerData filterContainerData,
                            ResourceContainerData exportedResourcesContainerData) {
    public static final StreamCodec<RegistryFriendlyByteBuf, InterfaceData> STREAM_CODEC = StreamCodec.composite(
        ResourceContainerData.STREAM_CODEC, InterfaceData::filterContainerData,
        ResourceContainerData.STREAM_CODEC, InterfaceData::exportedResourcesContainerData,
        InterfaceData::new
    );
}
