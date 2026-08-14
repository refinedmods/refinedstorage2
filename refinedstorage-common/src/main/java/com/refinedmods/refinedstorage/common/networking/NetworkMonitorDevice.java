package com.refinedmods.refinedstorage.common.networking;

import java.util.UUID;

import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;

public record NetworkMonitorDevice(UUID id, Component name, Holder<Item> item) {
    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkMonitorDevice> STREAM_CODEC =
        StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, NetworkMonitorDevice::id,
            ComponentSerialization.STREAM_CODEC, NetworkMonitorDevice::name,
            Item.STREAM_CODEC, NetworkMonitorDevice::item,
            NetworkMonitorDevice::new
        );
}
