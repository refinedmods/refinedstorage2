package com.refinedmods.refinedstorage2.platform.forge.internal.network.node.exporter;

import com.refinedmods.refinedstorage2.api.network.impl.node.exporter.AbstractExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.network.node.exporter.ExporterTransferStrategy;
import com.refinedmods.refinedstorage2.platform.api.network.node.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.api.network.node.exporter.ExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeState;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.internal.network.node.AbstractFuzzyExporterTransferStrategy;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.FluidHandlerInsertableStorage;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.InteractionCoordinates;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.InteractionCoordinatesImpl;

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
        final InteractionCoordinates coordinates = new InteractionCoordinatesImpl(level, pos, direction);
        final FluidHandlerInsertableStorage destination = new FluidHandlerInsertableStorage(
            coordinates,
            amountOverride
        );
        final long transferQuota = (upgradeState.hasUpgrade(Items.INSTANCE.getStackUpgrade()) ? 64 : 1)
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
