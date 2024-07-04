package com.refinedmods.refinedstorage.platform.neoforge.storage.externalstorage;

import com.refinedmods.refinedstorage.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage.platform.api.storage.externalstorage.PlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage.platform.neoforge.storage.CapabilityCacheImpl;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class FluidHandlerPlatformExternalStorageProviderFactory implements PlatformExternalStorageProviderFactory {
    @Override
    public Optional<ExternalStorageProvider> create(final ServerLevel level,
                                                    final BlockPos pos,
                                                    final Direction direction) {
        return Optional.of(new FluidHandlerExternalStorageProvider(new CapabilityCacheImpl(level, pos, direction)));
    }
}
