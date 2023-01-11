package com.refinedmods.refinedstorage2.platform.fabric.internal.network.node.externalstorage;

import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage2.platform.api.network.node.externalstorage.PlatformExternalStorageProviderFactory;

import java.util.Optional;
import java.util.function.Function;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class StoragePlatformExternalStorageProviderFactory<T, P> implements PlatformExternalStorageProviderFactory {
    private final BlockApiLookup<Storage<P>, Direction> lookup;
    private final Function<P, T> fromPlatformMapper;
    private final Function<T, P> toPlatformMapper;

    public StoragePlatformExternalStorageProviderFactory(final BlockApiLookup<Storage<P>, Direction> lookup,
                                                         final Function<P, T> fromPlatformMapper,
                                                         final Function<T, P> toPlatformMapper) {
        this.lookup = lookup;
        this.fromPlatformMapper = fromPlatformMapper;
        this.toPlatformMapper = toPlatformMapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> Optional<ExternalStorageProvider<E>> create(final ServerLevel level,
                                                           final BlockPos pos,
                                                           final Direction direction) {
        return Optional.of((ExternalStorageProvider<E>) new StorageExternalStorageProvider<>(
            lookup,
            fromPlatformMapper,
            toPlatformMapper,
            level,
            pos,
            direction
        ));
    }
}
