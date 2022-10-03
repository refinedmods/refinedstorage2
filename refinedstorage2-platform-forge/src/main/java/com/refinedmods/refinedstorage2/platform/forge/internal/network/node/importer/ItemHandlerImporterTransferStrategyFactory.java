package com.refinedmods.refinedstorage2.platform.forge.internal.network.node.importer;

import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterSource;
import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterTransferStrategyImpl;
import com.refinedmods.refinedstorage2.platform.api.network.node.importer.ImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.InteractionCoordinates;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class ItemHandlerImporterTransferStrategyFactory implements ImporterTransferStrategyFactory {
    @Override
    public ImporterTransferStrategy create(final ServerLevel level,
                                           final BlockPos pos,
                                           final Direction direction,
                                           final boolean hasStackUpgrade) {
        final ImporterSource<ItemResource> source = new ItemHandlerImporterSource(new InteractionCoordinates(
            level,
            pos,
            direction
        ));
        final int transferQuota = hasStackUpgrade ? 64 : 1;
        return new ImporterTransferStrategyImpl<>(source, StorageChannelTypes.ITEM, transferQuota);
    }
}
