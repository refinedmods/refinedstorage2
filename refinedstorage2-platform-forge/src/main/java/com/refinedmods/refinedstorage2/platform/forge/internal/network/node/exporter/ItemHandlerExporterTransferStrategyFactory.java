package com.refinedmods.refinedstorage2.platform.forge.internal.network.node.exporter;

import com.refinedmods.refinedstorage2.api.network.node.exporter.strategy.AbstractExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.network.node.exporter.strategy.ExporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.platform.api.network.node.exporter.ExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.internal.network.node.AbstractFuzzyExporterTransferStrategy;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.InteractionCoordinates;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.ItemHandlerInsertableStorage;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class ItemHandlerExporterTransferStrategyFactory implements ExporterTransferStrategyFactory {
    @Override
    public ExporterTransferStrategy create(final ServerLevel level,
                                           final BlockPos pos,
                                           final Direction direction,
                                           final boolean hasStackUpgrade,
                                           final boolean fuzzyMode) {
        final InsertableStorage<ItemResource> destination = new ItemHandlerInsertableStorage(new InteractionCoordinates(
            level,
            pos,
            direction
        ));
        final int transferQuota = hasStackUpgrade ? 64 : 1;
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
