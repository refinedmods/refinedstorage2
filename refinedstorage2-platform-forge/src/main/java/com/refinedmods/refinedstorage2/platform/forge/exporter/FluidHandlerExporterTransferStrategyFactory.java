package com.refinedmods.refinedstorage2.platform.forge.exporter;

import com.refinedmods.refinedstorage2.api.network.impl.node.exporter.AbstractExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.network.node.exporter.ExporterTransferStrategy;
import com.refinedmods.refinedstorage2.platform.api.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.api.exporter.ExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeState;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.exporter.AbstractFuzzyExporterTransferStrategy;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.forge.storage.CapabilityCache;
import com.refinedmods.refinedstorage2.platform.forge.storage.CapabilityCacheImpl;
import com.refinedmods.refinedstorage2.platform.forge.storage.FluidHandlerInsertableStorage;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class FluidHandlerExporterTransferStrategyFactory implements ExporterTransferStrategyFactory {
    @Override
    public ExporterTransferStrategy create(final ServerLevel level,
                                           final BlockPos pos,
                                           final Direction direction,
                                           final UpgradeState upgradeState,
                                           final AmountOverride amountOverride,
                                           final boolean fuzzyMode) {
        final CapabilityCache coordinates = new CapabilityCacheImpl(level, pos, direction);
        final FluidHandlerInsertableStorage destination = new FluidHandlerInsertableStorage(
            coordinates,
            amountOverride
        );
        final long transferQuota = (upgradeState.has(Items.INSTANCE.getStackUpgrade()) ? 64 : 1)
            * Platform.INSTANCE.getBucketAmount();
        if (fuzzyMode) {
            return new AbstractFuzzyExporterTransferStrategy<>(destination, StorageChannelTypes.FLUID, transferQuota) {
                @Nullable
                @Override
                protected FluidResource tryConvert(final Object resource) {
                    return resource instanceof FluidResource fluidResource ? fluidResource : null;
                }
            };
        }
        return new AbstractExporterTransferStrategy<>(destination, StorageChannelTypes.FLUID, transferQuota) {
            @Nullable
            @Override
            protected FluidResource tryConvert(final Object resource) {
                return resource instanceof FluidResource fluidResource ? fluidResource : null;
            }
        };
    }
}
