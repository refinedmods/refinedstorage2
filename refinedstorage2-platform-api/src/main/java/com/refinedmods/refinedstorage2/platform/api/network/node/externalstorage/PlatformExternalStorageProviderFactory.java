package com.refinedmods.refinedstorage2.platform.api.network.node.externalstorage;

import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public interface PlatformExternalStorageProviderFactory {
    <T> Optional<ExternalStorageProvider<T>> create(ServerLevel level, BlockPos pos, Direction direction);

    default int getPriority() {
        return 0;
    }
}
