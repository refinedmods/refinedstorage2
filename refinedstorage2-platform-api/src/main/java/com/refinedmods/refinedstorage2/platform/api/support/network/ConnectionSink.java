package com.refinedmods.refinedstorage2.platform.api.support.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.2")
public interface ConnectionSink {
    void tryConnect(GlobalPos pos);

    void tryConnectInSameDimension(BlockPos pos, Direction incomingDirection);
}
