package com.refinedmods.refinedstorage.common.iface;

import com.refinedmods.refinedstorage.common.support.exportingindicator.ExportingIndicator;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import static com.refinedmods.refinedstorage.common.util.PlatformUtil.enumStreamCodec;

public record InterfaceData(ResourceContainerData filterContainerData,
                            ResourceContainerData exportedResourcesContainerData,
                            List<ExportingIndicator> exportingIndicators) {
    public static final StreamCodec<RegistryFriendlyByteBuf, InterfaceData> STREAM_CODEC = StreamCodec.composite(
        ResourceContainerData.STREAM_CODEC, InterfaceData::filterContainerData,
        ResourceContainerData.STREAM_CODEC, InterfaceData::exportedResourcesContainerData,
        ByteBufCodecs.collection(ArrayList::new, enumStreamCodec(ExportingIndicator.values())),
        InterfaceData::exportingIndicators,
        InterfaceData::new
    );
}
