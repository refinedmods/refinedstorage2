package com.refinedmods.refinedstorage2.platform.common.iface;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage2.platform.api.storage.externalstorage.PlatformExternalStorageProviderFactory;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class InterfacePlatformExternalStorageProviderFactory implements PlatformExternalStorageProviderFactory {
    @Override
    public Optional<ExternalStorageProvider> create(final ServerLevel level,
                                                    final BlockPos pos,
                                                    final Direction direction,
                                                    final StorageChannelType storageChannelType) {
        if (!(level.getBlockEntity(pos) instanceof InterfaceBlockEntity)) {
            return Optional.empty();
        }
        return Optional.of(new InterfaceProxyExternalStorageProvider(level, pos, storageChannelType));
    }

    @Override
    public int getPriority() {
        return -1;
    }
}
