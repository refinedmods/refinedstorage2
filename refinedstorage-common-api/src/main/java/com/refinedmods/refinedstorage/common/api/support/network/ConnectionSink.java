package com.refinedmods.refinedstorage.common.api.support.network;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.block.Block;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.2")
public interface ConnectionSink {
    void tryConnect(GlobalPos pos, @Nullable Class<? extends Block> allowedBlockType);

    default void tryConnectInSameDimension(BlockPos pos, Direction incomingDirection) {
        tryConnectInSameDimension(pos, incomingDirection, null);
    }

    void tryConnectInSameDimension(BlockPos pos,
                                   Direction incomingDirection,
                                   @Nullable Class<? extends Block> allowedBlockType);
}
