package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.network.node.importer.CompositeImporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterTransferStrategy;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.importer.ImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.ImporterBlock;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImporterBlockEntity extends AbstractInternalNetworkNodeContainerBlockEntity<ImporterNetworkNode> {
    private static final Logger LOGGER = LogManager.getLogger();

    public ImporterBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getImporter(),
            pos,
            state,
            new ImporterNetworkNode(Platform.INSTANCE.getConfig().getImporter().getEnergyUsage())
        );
    }

    @Override
    public void setLevel(final Level level) {
        super.setLevel(level);
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        final Block block = getBlockState().getBlock();
        if (!(block instanceof ImporterBlock importerBlock)) {
            LOGGER.warn("Could not find importer block at {}, found {} instead", worldPosition, block);
            return;
        }
        final Direction direction = importerBlock.getDirection(getBlockState());
        if (direction == null) {
            LOGGER.warn(
                "Could not extract direction from importer block at {}, state is {}",
                worldPosition,
                getBlockState()
            );
            return;
        }
        final CompositeImporterTransferStrategy strategy = createStrategy(serverLevel, direction);
        LOGGER.info("Initialized importer at {} with strategy {}", worldPosition, strategy);
        getNode().setTransferStrategy(strategy);
    }

    private CompositeImporterTransferStrategy createStrategy(final ServerLevel serverLevel,
                                                             final Direction direction) {
        final Direction incomingDirection = direction.getOpposite();
        // TODO: does this need to be invalidated?
        final BlockPos sourcePosition = worldPosition.offset(direction.getNormal());
        final List<ImporterTransferStrategyFactory> factories =
            PlatformApi.INSTANCE.getImporterTransferStrategyRegistry().getAll();
        final List<ImporterTransferStrategy> strategies = factories
            .stream()
            .map(factory -> factory.create(serverLevel, sourcePosition, incomingDirection, getNode()))
            .toList();
        return new CompositeImporterTransferStrategy(strategies);
    }
}
