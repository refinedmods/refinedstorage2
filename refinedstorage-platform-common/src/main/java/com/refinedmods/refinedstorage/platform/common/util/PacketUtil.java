package com.refinedmods.refinedstorage.platform.common.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class PacketUtil {
    private PacketUtil() {
    }

    public static <T extends Enum<T>> StreamCodec<ByteBuf, T> enumStreamCodec(final T[] values) {
        return ByteBufCodecs.idMapper(id -> id < 0 || id >= values.length ? values[0] : values[id], Enum::ordinal);
    }
}
