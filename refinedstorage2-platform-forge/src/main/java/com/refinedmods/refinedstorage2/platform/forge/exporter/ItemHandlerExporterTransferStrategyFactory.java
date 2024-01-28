package com.refinedmods.refinedstorage2.platform.forge.exporter;

import com.refinedmods.refinedstorage2.api.network.impl.node.exporter.AbstractExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.network.node.exporter.ExporterTransferStrategy;
import com.refinedmods.refinedstorage2.platform.api.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.api.exporter.ExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeState;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.exporter.AbstractFuzzyExporterTransferStrategy;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.forge.storage.CapabilityCacheImpl;
import com.refinedmods.refinedstorage2.platform.forge.storage.ItemHandlerInsertableStorage;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class ItemHandlerExporterTransferStrategyFactory implements ExporterTransferStrategyFactory {
    @Override
    public ExporterTransferStrategy create(final ServerLevel level,
                                           final BlockPos pos,
                                           final Direction direction,
                                           final UpgradeState upgradeState,
                                           final AmountOverride amountOverride,
                                           final boolean fuzzyMode) {
        final CapabilityCacheImpl coordinates = new CapabilityCacheImpl(level, pos, direction);
        final ItemHandlerInsertableStorage destination = new ItemHandlerInsertableStorage(coordinates, amountOverride);
        final int transferQuota = upgradeState.has(Items.INSTANCE.getStackUpgrade()) ? 64 : 1;
        if (fuzzyMode) {
            return new AbstractFuzzyExporterTransferStrategy<>(destination, StorageChannelTypes.ITEM, transferQuota) {
                @Nullable
                @Override
                protected ItemResource tryConvert(final Object resource) {
                    return resource instanceof ItemResource itemResource ? itemResource : null;
                }
            };
        }
        return new AbstractExporterTransferStrategy<>(destination, StorageChannelTypes.ITEM, transferQuota) {
            @Nullable
            @Override
            protected ItemResource tryConvert(final Object resource) {
                return resource instanceof ItemResource itemResource ? itemResource : null;
            }
        };
    }
}
