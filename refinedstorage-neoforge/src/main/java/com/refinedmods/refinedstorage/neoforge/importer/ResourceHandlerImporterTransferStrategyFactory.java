package com.refinedmods.refinedstorage.neoforge.importer;

import com.refinedmods.refinedstorage.api.network.impl.node.importer.ImporterTransferStrategyImpl;
import com.refinedmods.refinedstorage.api.network.node.importer.ImporterTransferStrategy;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.importer.ImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeState;
import com.refinedmods.refinedstorage.common.importer.ImporterTransferQuotaProvider;
import com.refinedmods.refinedstorage.neoforge.storage.CapabilityCacheResourceHandlerProvider;

import java.util.Optional;
import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.Nullable;

public class ResourceHandlerImporterTransferStrategyFactory<T extends Resource>
    implements ImporterTransferStrategyFactory {
    private final long singleAmount;
    private final Function<ResourceKey, Optional<T>> toPlatformMapper;
    private final Function<T, ResourceKey> fromPlatformMapper;
    private final BlockCapability<ResourceHandler<T>, @Nullable Direction> capability;

    public ResourceHandlerImporterTransferStrategyFactory(final long singleAmount,
                                                          final Function<ResourceKey, Optional<T>> toPlatformMapper,
                                                          final Function<T, ResourceKey> fromPlatformMapper,
                                                          final BlockCapability<ResourceHandler<T>, @Nullable Direction>
                                                              capability) {
        this.singleAmount = singleAmount;
        this.toPlatformMapper = toPlatformMapper;
        this.fromPlatformMapper = fromPlatformMapper;
        this.capability = capability;
    }

    @Override
    public ImporterTransferStrategy create(final ServerLevel level,
                                           final BlockPos pos,
                                           final Direction direction,
                                           final UpgradeState upgradeState) {
        final CapabilityCacheResourceHandlerProvider<T> provider = new CapabilityCacheResourceHandlerProvider<>(
            level,
            pos,
            direction,
            capability,
            fromPlatformMapper
        );
        final ResourceHandlerImporterSource<T> source = new ResourceHandlerImporterSource<>(provider, toPlatformMapper);
        final ImporterTransferQuotaProvider transferQuotaProvider = new ImporterTransferQuotaProvider(
            singleAmount,
            upgradeState,
            source::getAmount
        );
        return new ImporterTransferStrategyImpl(source, transferQuotaProvider);
    }
}
