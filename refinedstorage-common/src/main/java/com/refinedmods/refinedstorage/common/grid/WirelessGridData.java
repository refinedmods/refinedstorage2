package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record WirelessGridData(GridData gridData, PlayerSlotReference playerSlotReference) {
    public static final StreamCodec<RegistryFriendlyByteBuf, WirelessGridData> STREAM_CODEC = StreamCodec.composite(
        GridData.STREAM_CODEC, WirelessGridData::gridData,
        PlayerSlotReference.STREAM_CODEC, WirelessGridData::playerSlotReference,
        WirelessGridData::new
    );
}
