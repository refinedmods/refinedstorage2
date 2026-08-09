package com.refinedmods.refinedstorage.common.api.networking;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "3.3.0")
public record NetworkMonitorDeviceType(Component name, Holder<Item> icon) {
    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkMonitorDeviceType> STREAM_CODEC =
        StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC, NetworkMonitorDeviceType::name,
            Item.STREAM_CODEC, NetworkMonitorDeviceType::icon,
            NetworkMonitorDeviceType::new
        );
}
