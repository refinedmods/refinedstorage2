package com.refinedmods.refinedstorage2.platform.fabric.storage.externalstorage;

import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage2.platform.api.storage.externalstorage.PlatformExternalStorageProviderFactory;

import java.util.Optional;
import java.util.function.Function;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class FabricStoragePlatformExternalStorageProviderFactory<T>
    implements PlatformExternalStorageProviderFactory {
    private final StorageChannelType theStorageChannelType;
    private final BlockApiLookup<Storage<T>, Direction> lookup;
    private final Function<T, ResourceKey> fromPlatformMapper;
    private final Function<ResourceKey, T> toPlatformMapper;

    public FabricStoragePlatformExternalStorageProviderFactory(final StorageChannelType storageChannelType,
                                                               final BlockApiLookup<Storage<T>, Direction> lookup,
                                                               final Function<T, ResourceKey> fromPlatformMapper,
                                                               final Function<ResourceKey, T> toPlatformMapper) {
        this.theStorageChannelType = storageChannelType;
        this.lookup = lookup;
        this.fromPlatformMapper = fromPlatformMapper;
        this.toPlatformMapper = toPlatformMapper;
    }

    @Override
    public Optional<ExternalStorageProvider> create(final ServerLevel level,
                                                    final BlockPos pos,
                                                    final Direction direction,
                                                    final StorageChannelType storageChannelType) {
        if (storageChannelType != theStorageChannelType) {
            return Optional.empty();
        }
        return Optional.of(new FabricStorageExternalStorageProvider<>(
            lookup,
            fromPlatformMapper,
            toPlatformMapper,
            level,
            pos,
            direction
        ));
    }
}
