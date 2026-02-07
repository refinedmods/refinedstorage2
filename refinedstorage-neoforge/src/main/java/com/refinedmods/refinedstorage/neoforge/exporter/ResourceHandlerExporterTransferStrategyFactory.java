package com.refinedmods.refinedstorage.neoforge.exporter;

import com.refinedmods.refinedstorage.api.network.impl.node.exporter.ExporterTransferStrategyImpl;
import com.refinedmods.refinedstorage.api.network.impl.node.exporter.MissingResourcesListeningExporterTransferStrategy;
import com.refinedmods.refinedstorage.api.network.node.exporter.ExporterTransferStrategy;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage.common.api.exporter.ExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage.common.api.storage.root.FuzzyRootStorage;
import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeState;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.exporter.ExporterTransferQuotaProvider;
import com.refinedmods.refinedstorage.neoforge.storage.CapabilityCacheResourceHandlerProvider;
import com.refinedmods.refinedstorage.neoforge.storage.ResourceHandlerInsertableStorage;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToLongFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.api.network.impl.node.exporter.MissingResourcesListeningExporterTransferStrategy.OnMissingResources.scheduleAutocrafting;

public class ResourceHandlerExporterTransferStrategyFactory<T extends Resource>
    implements ExporterTransferStrategyFactory {
    private final long singleAmount;
    private final Class<? extends ResourceKey> resourceKeyType;
    private final Function<ResourceKey, Optional<T>> toPlatformMapper;
    private final Function<T, ResourceKey> fromPlatformMapper;
    private final BlockCapability<ResourceHandler<T>, @Nullable Direction> capability;

    public ResourceHandlerExporterTransferStrategyFactory(final long singleAmount,
                                                          final Class<? extends ResourceKey> resourceKeyType,
                                                          final Function<ResourceKey, Optional<T>> toPlatformMapper,
                                                          final Function<T, ResourceKey> fromPlatformMapper,
                                                          final BlockCapability<ResourceHandler<T>, @Nullable Direction>
                                                              capability) {
        this.singleAmount = singleAmount;
        this.resourceKeyType = resourceKeyType;
        this.toPlatformMapper = toPlatformMapper;
        this.fromPlatformMapper = fromPlatformMapper;
        this.capability = capability;
    }

    @Override
    public Class<? extends ResourceKey> getResourceType() {
        return resourceKeyType;
    }

    @Override
    public ExporterTransferStrategy create(final ServerLevel level,
                                           final BlockPos pos,
                                           final Direction direction,
                                           final UpgradeState upgradeState,
                                           final boolean fuzzyMode) {
        final CapabilityCacheResourceHandlerProvider<T> provider = new CapabilityCacheResourceHandlerProvider<>(
            level,
            pos,
            direction,
            capability,
            fromPlatformMapper
        );
        final ResourceHandlerInsertableStorage<T> destination = new ResourceHandlerInsertableStorage<>(
            provider,
            toPlatformMapper
        );
        final ExporterTransferStrategy strategy = create(
            fuzzyMode,
            destination,
            new ExporterTransferQuotaProvider(singleAmount, upgradeState, destination::getAmount, true)
        );
        if (upgradeState.has(Items.INSTANCE.getAutocraftingUpgrade())) {
            return new MissingResourcesListeningExporterTransferStrategy(strategy, scheduleAutocrafting(
                new ExporterTransferQuotaProvider(singleAmount, upgradeState, destination::getAmount, false)));
        }
        return strategy;
    }

    private ExporterTransferStrategy create(final boolean fuzzyMode,
                                            final InsertableStorage destination,
                                            final ToLongFunction<ResourceKey> transferQuotaProvider) {
        if (fuzzyMode) {
            return new ExporterTransferStrategyImpl(destination, transferQuotaProvider, FuzzyRootStorage.expander());
        }
        return new ExporterTransferStrategyImpl(destination, transferQuotaProvider);
    }
}
