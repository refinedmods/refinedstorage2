package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;

import java.util.Optional;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PortableGridData(GridData gridData, long stored, long capacity,
                               Optional<PlayerSlotReference> slotReference) {
    public static final StreamCodec<RegistryFriendlyByteBuf, PortableGridData> STREAM_CODEC = StreamCodec.composite(
        GridData.STREAM_CODEC, PortableGridData::gridData,
        ByteBufCodecs.VAR_LONG, PortableGridData::stored,
        ByteBufCodecs.VAR_LONG, PortableGridData::capacity,
        ByteBufCodecs.optional(PlayerSlotReference.STREAM_CODEC), PortableGridData::slotReference,
        PortableGridData::new
    );
}
