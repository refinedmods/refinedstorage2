package com.refinedmods.refinedstorage.platform.common.support.containermenu;

import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReferenceFactory;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceContainerData;

import java.util.Optional;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SingleAmountData(Optional<SlotReference> slotReference,
                               double amount,
                               ResourceContainerData resourceContainerData) {
    public static final StreamCodec<RegistryFriendlyByteBuf, SingleAmountData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(SlotReferenceFactory.STREAM_CODEC), SingleAmountData::slotReference,
        ByteBufCodecs.DOUBLE, SingleAmountData::amount,
        ResourceContainerData.STREAM_CODEC, SingleAmountData::resourceContainerData,
        SingleAmountData::new
    );
}
