package com.refinedmods.refinedstorage2.platform.common.internal.network.node.externalstorage;

import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage2.platform.api.network.node.externalstorage.PlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class InterfacePlatformExternalStorageProviderFactory implements PlatformExternalStorageProviderFactory {
    @Override
    public <T> Optional<ExternalStorageProvider<T>> create(final ServerLevel level,
                                                           final BlockPos pos,
                                                           final Direction direction) {
        if (level.getBlockState(pos).getBlock() != Blocks.INSTANCE.getInterface()) {
            return Optional.empty();
        }
        return Optional.of(new InterfaceProxyExternalStorageProvider<>(level, pos));
    }

    @Override
    public int getPriority() {
        return -1;
    }
}
