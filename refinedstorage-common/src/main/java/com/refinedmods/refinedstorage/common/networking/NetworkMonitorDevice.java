package com.refinedmods.refinedstorage.common.networking;

import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;

import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

public record NetworkMonitorDevice(UUID id, Component name, long energyUsage,
                                   OptionalInt insertPriority, OptionalInt extractPriority, Holder<Item> item) {
    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkMonitorDevice> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, NetworkMonitorDevice::id,
        ComponentSerialization.STREAM_CODEC, NetworkMonitorDevice::name,
        ByteBufCodecs.LONG, NetworkMonitorDevice::energyUsage,
        ByteBufCodecs.OPTIONAL_VAR_INT, NetworkMonitorDevice::insertPriority,
        ByteBufCodecs.OPTIONAL_VAR_INT, NetworkMonitorDevice::extractPriority,
        Item.STREAM_CODEC, NetworkMonitorDevice::item,
        NetworkMonitorDevice::new
    );

    @Override
    public boolean equals(@Nullable final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final NetworkMonitorDevice that = (NetworkMonitorDevice) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
