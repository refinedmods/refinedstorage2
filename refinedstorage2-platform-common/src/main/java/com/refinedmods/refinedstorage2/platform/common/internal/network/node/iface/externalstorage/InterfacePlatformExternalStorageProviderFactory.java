package com.refinedmods.refinedstorage2.platform.common.internal.network.node.iface.externalstorage;

import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage2.platform.api.network.node.externalstorage.PlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.platform.common.block.entity.InterfaceBlockEntity;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class InterfacePlatformExternalStorageProviderFactory implements PlatformExternalStorageProviderFactory {
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<ExternalStorageProvider<T>> create(final ServerLevel level,
                                                           final BlockPos pos,
                                                           final Direction direction) {
        if (!(level.getBlockEntity(pos) instanceof InterfaceBlockEntity)) {
            return Optional.empty();
        }
        return Optional.of((ExternalStorageProvider<T>) new InterfaceProxyExternalStorageProvider(level, pos));
    }

    @Override
    public int getPriority() {
        return -1;
    }
}
