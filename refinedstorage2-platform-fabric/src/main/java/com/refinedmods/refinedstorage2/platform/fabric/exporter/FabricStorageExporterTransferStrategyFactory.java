package com.refinedmods.refinedstorage2.platform.fabric.exporter;

import com.refinedmods.refinedstorage2.api.network.impl.node.exporter.ExporterTransferStrategyImpl;
import com.refinedmods.refinedstorage2.api.network.node.exporter.ExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.platform.api.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.api.exporter.ExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeState;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.exporter.FuzzyExporterTransferStrategy;
import com.refinedmods.refinedstorage2.platform.fabric.storage.FabricStorageInsertableStorage;

import java.util.function.Function;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class FabricStorageExporterTransferStrategyFactory<T> implements ExporterTransferStrategyFactory {
    private final BlockApiLookup<Storage<T>, Direction> lookup;
    private final Function<ResourceKey, T> toPlatformMapper;
    private final long singleAmount;

    public FabricStorageExporterTransferStrategyFactory(final BlockApiLookup<Storage<T>, Direction> lookup,
                                                        final Function<ResourceKey, T> toPlatformMapper,
                                                        final long singleAmount) {
        this.lookup = lookup;
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
        final FabricStorageInsertableStorage<T> insertTarget = new FabricStorageInsertableStorage<>(
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

    private ExporterTransferStrategyImpl create(final boolean fuzzyMode,
                                                final FabricStorageInsertableStorage<T> insertTarget,
                                                final long transferQuota) {
        if (fuzzyMode) {
            return new FuzzyExporterTransferStrategy(insertTarget, transferQuota);
        }
        return new ExporterTransferStrategyImpl(insertTarget, transferQuota);
    }
}
