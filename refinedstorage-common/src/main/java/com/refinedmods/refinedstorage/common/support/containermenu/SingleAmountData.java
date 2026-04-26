package com.refinedmods.refinedstorage.common.support.containermenu;

import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;

import java.util.Optional;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SingleAmountData(Optional<PlayerSlotReference> slotReference,
                               double amount,
                               ResourceContainerData resourceContainerData) {
    public static final StreamCodec<RegistryFriendlyByteBuf, SingleAmountData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(PlayerSlotReference.STREAM_CODEC), SingleAmountData::slotReference,
        ByteBufCodecs.DOUBLE, SingleAmountData::amount,
        ResourceContainerData.STREAM_CODEC, SingleAmountData::resourceContainerData,
        SingleAmountData::new
    );
}
