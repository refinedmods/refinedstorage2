package com.refinedmods.refinedstorage2.platform.fabric.exporter;

import com.refinedmods.refinedstorage2.api.network.impl.node.exporter.AbstractExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.network.node.exporter.ExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.api.exporter.ExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeState;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.exporter.AbstractFuzzyExporterTransferStrategy;
import com.refinedmods.refinedstorage2.platform.fabric.storage.FabricStorageInsertableStorage;

import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class FabricStorageExporterTransferStrategyFactory<T, P> implements ExporterTransferStrategyFactory {
    private final BlockApiLookup<Storage<P>, Direction> lookup;
    private final StorageChannelType<T> storageChannelType;
    private final Function<T, P> toPlatformMapper;
    private final Function<Object, Optional<T>> mapper;
    private final long singleAmount;

    public FabricStorageExporterTransferStrategyFactory(final BlockApiLookup<Storage<P>, Direction> lookup,
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
                                           final UpgradeState upgradeState,
                                           final AmountOverride amountOverride,
                                           final boolean fuzzyMode) {
        final FabricStorageInsertableStorage<T, P> insertTarget = new FabricStorageInsertableStorage<>(
            lookup,
            toPlatformMapper,
            level,
            pos,
            direction,
            amountOverride
        );
        final long transferQuota = upgradeState.has(Items.INSTANCE.getStackUpgrade())
            ? singleAmount * 64
            : singleAmount;
        return create(fuzzyMode, insertTarget, transferQuota);
    }

    private AbstractExporterTransferStrategy<T> create(final boolean fuzzyMode,
                                                       final FabricStorageInsertableStorage<T, P> insertTarget,
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
