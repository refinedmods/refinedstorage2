package com.refinedmods.refinedstorage.platform.common.storage;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractStorageContainerNetworkNode;
import com.refinedmods.refinedstorage.platform.common.support.network.NetworkNodeBlockEntityTicker;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class DiskContainerBlockEntityTicker
    <T extends AbstractStorageContainerNetworkNode, B extends AbstractDiskContainerBlockEntity<T>>
    extends NetworkNodeBlockEntityTicker<T, B> {

    public DiskContainerBlockEntityTicker(
        final Supplier<BlockEntityType<B>> allowedTypeSupplier
    ) {
        super(allowedTypeSupplier);
    }

    public DiskContainerBlockEntityTicker(final Supplier<BlockEntityType<B>> allowedTypeSupplier,
                                          @Nullable final BooleanProperty activenessProperty) {
        super(allowedTypeSupplier, activenessProperty);
    }

    @Override
    public void tick(final Level level,
                     final BlockPos pos,
                     final BlockState state,
                     final B blockEntity) {
        super.tick(level, pos, state, blockEntity);
        blockEntity.updateDiskStateIfNecessaryInLevel();
    }
}
