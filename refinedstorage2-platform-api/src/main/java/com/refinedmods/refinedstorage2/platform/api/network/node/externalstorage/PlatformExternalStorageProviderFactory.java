package com.refinedmods.refinedstorage2.platform.api.network.node.externalstorage;

import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public interface PlatformExternalStorageProviderFactory {
    <T> Optional<ExternalStorageProvider<T>> create(ServerLevel level, BlockPos pos, Direction direction);

    default int getPriority() {
        return 0;
    }
}
