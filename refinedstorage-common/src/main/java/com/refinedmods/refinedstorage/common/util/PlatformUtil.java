package com.refinedmods.refinedstorage.common.util;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public final class PlatformUtil {
    private PlatformUtil() {
    }

    public static <T extends Enum<T>> StreamCodec<ByteBuf, T> enumStreamCodec(final T[] values) {
        return ByteBufCodecs.idMapper(id -> id < 0 || id >= values.length ? values[0] : values[id], Enum::ordinal);
    }

    public static void sendBlockUpdateToClient(@Nullable final Level level, final BlockPos pos) {
        if (level == null) {
            return;
        }
        if (level.isClientSide()) {
            throw new IllegalArgumentException("Cannot send block update to client from client");
        }
        level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), Block.UPDATE_ALL);
    }
}
