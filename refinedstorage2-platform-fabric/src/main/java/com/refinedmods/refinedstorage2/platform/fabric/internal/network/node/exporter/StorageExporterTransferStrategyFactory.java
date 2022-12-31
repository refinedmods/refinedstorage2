package com.refinedmods.refinedstorage2.platform.fabric.internal.network.node.exporter;

import com.refinedmods.refinedstorage2.api.network.impl.node.exporter.strategy.AbstractExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.network.node.exporter.strategy.ExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.network.node.exporter.ExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.common.internal.network.node.AbstractFuzzyExporterTransferStrategy;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.StorageInsertableStorage;

import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class StorageExporterTransferStrategyFactory<T, P> implements ExporterTransferStrategyFactory {
    private final BlockApiLookup<Storage<P>, Direction> lookup;
    private final StorageChannelType<T> storageChannelType;
    private final Function<T, P> toPlatformMapper;
    private final Function<Object, Optional<T>> mapper;
    private final long singleAmount;

    public StorageExporterTransferStrategyFactory(final BlockApiLookup<Storage<P>, Direction> lookup,
                                                  final StorageChannelType<T> storageChannelType,
                                                  final Function<Object, Optional<T>> mapper,
                                                  final Function<T, P> toPlatformMapper,
                                                  final long singleAmount) {
        this.lookup = lookup;
        this.storageChannelType = storageChannelType;
        this.mapper = mapper;
        this.toPlatformMapper = toPlatformMapper;
        this.singleAmount = singleAmount;
    }

    @Override
    public ExporterTransferStrategy create(final ServerLevel level,
                                           final BlockPos pos,
                                           final Direction direction,
                                           final boolean hasStackUpgrade,
                                           final boolean fuzzyMode) {
        final StorageInsertableStorage<T, P> insertTarget = new StorageInsertableStorage<>(
            lookup,
            toPlatformMapper,
            level,
            pos,
            direction
        );
        final long transferQuota = hasStackUpgrade ? singleAmount * 64 : singleAmount;
        return create(fuzzyMode, insertTarget, transferQuota);
    }

    private AbstractExporterTransferStrategy<T> create(final boolean fuzzyMode,
                                                       final StorageInsertableStorage<T, P> insertTarget,
                                                       final long transferQuota) {
        if (fuzzyMode) {
            return new AbstractFuzzyExporterTransferStrategy<>(insertTarget, storageChannelType, transferQuota) {
                @Nullable
                @Override
                protected T tryConvert(final Object resource) {
                    return mapper.apply(resource).orElse(null);
                }
            };
        }

        return new AbstractExporterTransferStrategy<>(insertTarget, storageChannelType, transferQuota) {
            @Nullable
            @Override
            protected T tryConvert(final Object resource) {
                return mapper.apply(resource).orElse(null);
            }
        };
    }
}
