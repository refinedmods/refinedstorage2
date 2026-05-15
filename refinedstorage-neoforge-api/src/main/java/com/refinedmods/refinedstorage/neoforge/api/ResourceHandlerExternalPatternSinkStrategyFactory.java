package com.refinedmods.refinedstorage.neoforge.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "3.0.0")
@FunctionalInterface
public interface ResourceHandlerExternalPatternSinkStrategyFactory {
    ResourceHandlerExternalPatternSinkStrategy create(ServerLevel level, BlockPos pos, Direction direction);
}
