package com.refinedmods.refinedstorage2.platform.forge.internal.network.node.externalstorage;

import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage2.platform.api.network.node.externalstorage.PlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.InteractionCoordinatesImpl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class FluidHandlerPlatformExternalStorageProviderFactory implements PlatformExternalStorageProviderFactory {
    @Override
    @SuppressWarnings("unchecked")
    public <T> ExternalStorageProvider<T> create(final ServerLevel level,
                                                 final BlockPos pos,
                                                 final Direction direction) {
        return (ExternalStorageProvider<T>) new FluidHandlerExternalStorageProvider(
            new InteractionCoordinatesImpl(level, pos, direction)
        );
    }
}
