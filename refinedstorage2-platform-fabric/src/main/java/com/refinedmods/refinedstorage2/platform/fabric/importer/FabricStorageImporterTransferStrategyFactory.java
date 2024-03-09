package com.refinedmods.refinedstorage2.platform.fabric.importer;

import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterSource;
import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterTransferStrategyImpl;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.platform.api.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.api.importer.ImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeState;
import com.refinedmods.refinedstorage2.platform.common.content.Items;

import java.util.function.Function;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class FabricStorageImporterTransferStrategyFactory<P> implements ImporterTransferStrategyFactory {
    private final BlockApiLookup<Storage<P>, Direction> lookup;
    private final Function<P, ResourceKey> fromPlatformMapper;
    private final Function<ResourceKey, P> toPlatformMapper;
    private final long singleAmount;

    public FabricStorageImporterTransferStrategyFactory(final BlockApiLookup<Storage<P>, Direction> lookup,
                                                        final Function<P, ResourceKey> fromPlatformMapper,
                                                        final Function<ResourceKey, P> toPlatformMapper,
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
                                           final UpgradeState upgradeState,
                                           final AmountOverride amountOverride) {
        final ImporterSource source = new FabricStorageImporterSource<>(
            lookup,
            fromPlatformMapper,
            toPlatformMapper,
            level,
            pos,
            direction,
            amountOverride
        );
        final long transferQuota = upgradeState.has(Items.INSTANCE.getStackUpgrade())
            ? singleAmount * 64
            : singleAmount;
        return new ImporterTransferStrategyImpl(source, transferQuota);
    }
}
