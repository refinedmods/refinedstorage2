package com.refinedmods.refinedstorage.fabric.importer;

import com.refinedmods.refinedstorage.api.network.impl.node.importer.ImporterTransferStrategyImpl;
import com.refinedmods.refinedstorage.api.network.node.importer.ImporterTransferStrategy;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.importer.ImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeState;
import com.refinedmods.refinedstorage.common.importer.ImporterTransferQuotaProvider;

import java.util.function.Function;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;

public class FabricStorageImporterTransferStrategyFactory<P> implements ImporterTransferStrategyFactory {
    private final BlockApiLookup<Storage<P>, Direction> lookup;
    private final Function<P, ResourceKey> fromPlatformMapper;
    private final Function<ResourceKey, @Nullable P> toPlatformMapper;
    private final long singleAmount;

    public FabricStorageImporterTransferStrategyFactory(final BlockApiLookup<Storage<P>, Direction> lookup,
                                                        final Function<P, ResourceKey> fromPlatformMapper,
                                                        final Function<ResourceKey, @Nullable P> toPlatformMapper,
                                                        final long singleAmount) {
        this.lookup = lookup;
        this.fromPlatformMapper = fromPlatformMapper;
        this.toPlatformMapper = toPlatformMapper;
        this.singleAmount = singleAmount;
    }

    @Override
    public ImporterTransferStrategy create(final ServerLevel level,
                                           final BlockPos pos,
                                           final Direction direction,
                                           final UpgradeState upgradeState) {
        final FabricStorageImporterSource<P> source = new FabricStorageImporterSource<>(
            lookup,
            fromPlatformMapper,
            toPlatformMapper,
            level,
            pos,
            direction
        );
        final ImporterTransferQuotaProvider transferQuotaProvider = new ImporterTransferQuotaProvider(
            singleAmount,
            upgradeState,
            source::getAmount
        );
        return new ImporterTransferStrategyImpl(source, transferQuotaProvider);
    }
}
