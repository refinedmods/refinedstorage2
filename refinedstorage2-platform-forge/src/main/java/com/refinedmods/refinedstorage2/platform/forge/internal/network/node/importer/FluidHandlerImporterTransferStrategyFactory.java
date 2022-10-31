package com.refinedmods.refinedstorage2.platform.forge.internal.network.node.importer;

import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterSource;
import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterTransferStrategyImpl;
import com.refinedmods.refinedstorage2.platform.api.network.node.importer.ImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.InteractionCoordinatesImpl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fluids.FluidType;

public class FluidHandlerImporterTransferStrategyFactory implements ImporterTransferStrategyFactory {
    @Override
    public ImporterTransferStrategy create(final ServerLevel level,
                                           final BlockPos pos,
                                           final Direction direction,
                                           final boolean hasStackUpgrade) {
        final ImporterSource<FluidResource> source = new FluidHandlerImporterSource(new InteractionCoordinatesImpl(
            level,
            pos,
            direction
        ));
        final int transferQuota = hasStackUpgrade ? FluidType.BUCKET_VOLUME * 64 : FluidType.BUCKET_VOLUME;
        return new ImporterTransferStrategyImpl<>(source, StorageChannelTypes.FLUID, transferQuota);
    }
}
