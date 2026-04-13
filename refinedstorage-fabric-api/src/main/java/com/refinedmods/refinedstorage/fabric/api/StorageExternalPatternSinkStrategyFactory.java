package com.refinedmods.refinedstorage.fabric.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.apiguardian.api.API;

@FunctionalInterface
@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public interface StorageExternalPatternSinkStrategyFactory {
    StorageExternalPatternSinkStrategy create(ServerLevel level, BlockPos pos, Direction direction);
}
