package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractUpgradeableLevelInteractingNetworkNodeContainerBlockEntity<T extends AbstractNetworkNode>
    extends AbstractUpgradeableNetworkNodeContainerBlockEntity<T>
    implements BlockEntityWithDrops {
    private static final Logger LOGGER = LogManager.getLogger();

    protected AbstractUpgradeableLevelInteractingNetworkNodeContainerBlockEntity(
        final BlockEntityType<?> type,
        final BlockPos pos,
        final BlockState state,
        final T node,
        final UpgradeDestinations destination
    ) {
        super(type, pos, state, node, destination);
    }

    // used to handle rotations
    @Override
    @SuppressWarnings("deprecation")
    public void setBlockState(final BlockState newBlockState) {
        super.setBlockState(newBlockState);
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        initialize(serverLevel);
    }

    @Override
    public void setLevel(final Level level) {
        super.setLevel(level);
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        initialize(serverLevel);
    }

    @Override
    protected void upgradeContainerChanged() {
        super.upgradeContainerChanged();
        if (level instanceof ServerLevel serverLevel) {
            initialize(serverLevel);
        }
    }

    protected final void initialize(final ServerLevel level) {
        final Direction direction = getDirection();
        if (direction == null) {
            LOGGER.warn(
                "Failed to initialize: could not extract direction from block at {}, state is {}",
                worldPosition,
                getBlockState()
            );
            return;
        }
        initialize(level, direction);
    }

    protected abstract void initialize(ServerLevel level, Direction direction);
}
