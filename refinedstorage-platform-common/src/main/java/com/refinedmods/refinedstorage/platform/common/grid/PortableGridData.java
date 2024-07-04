package com.refinedmods.refinedstorage.platform.common.grid;

import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReferenceFactory;

import java.util.Optional;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PortableGridData(GridData gridData, long stored, long capacity, Optional<SlotReference> slotReference) {
    public static final StreamCodec<RegistryFriendlyByteBuf, PortableGridData> STREAM_CODEC = StreamCodec.composite(
        GridData.STREAM_CODEC, PortableGridData::gridData,
        ByteBufCodecs.VAR_LONG, PortableGridData::stored,
        ByteBufCodecs.VAR_LONG, PortableGridData::capacity,
        ByteBufCodecs.optional(SlotReferenceFactory.STREAM_CODEC), PortableGridData::slotReference,
        PortableGridData::new
    );
}
