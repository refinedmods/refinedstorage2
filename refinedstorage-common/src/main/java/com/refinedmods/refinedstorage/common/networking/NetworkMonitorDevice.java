package com.refinedmods.refinedstorage.common.networking;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record NetworkMonitorDevice(UUID id, Component name, ItemStack icon) {
    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkMonitorDevice> STREAM_CODEC =
        StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, NetworkMonitorDevice::id,
            ComponentSerialization.STREAM_CODEC, NetworkMonitorDevice::name,
            ItemStack.STREAM_CODEC, NetworkMonitorDevice::icon,
            NetworkMonitorDevice::new
        );
}
