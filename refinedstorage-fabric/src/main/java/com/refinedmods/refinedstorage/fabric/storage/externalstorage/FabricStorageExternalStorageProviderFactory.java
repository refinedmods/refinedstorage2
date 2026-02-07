package com.refinedmods.refinedstorage.fabric.storage.externalstorage;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage.common.api.storage.externalstorage.ExternalStorageProviderFactory;

import java.util.function.Function;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;

public class FabricStorageExternalStorageProviderFactory<T>
    implements ExternalStorageProviderFactory {
    private final BlockApiLookup<Storage<T>, Direction> lookup;
    private final Function<T, ResourceKey> fromPlatformMapper;
    private final Function<ResourceKey, @Nullable T> toPlatformMapper;

    public FabricStorageExternalStorageProviderFactory(final BlockApiLookup<Storage<T>, Direction> lookup,
                                                       final Function<T, ResourceKey> fromPlatformMapper,
                                                       final Function<ResourceKey, @Nullable T> toPlatformMapper) {
        this.lookup = lookup;
        this.fromPlatformMapper = fromPlatformMapper;
        this.toPlatformMapper = toPlatformMapper;
    }

    @Override
    public ExternalStorageProvider create(final ServerLevel level,
                                          final BlockPos pos,
                                          final Direction direction) {
        return new FabricStorageExternalStorageProvider<>(
            lookup,
            fromPlatformMapper,
            toPlatformMapper,
            level,
            pos,
            direction
        );
    }
}
