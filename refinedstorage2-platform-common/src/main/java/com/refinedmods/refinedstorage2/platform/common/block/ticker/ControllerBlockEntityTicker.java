package com.refinedmods.refinedstorage2.platform.common.block.ticker;

import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ControllerBlockEntity;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ControllerBlockEntityTicker
    extends NetworkNodeBlockEntityTicker<ControllerNetworkNode, ControllerBlockEntity> {
    public ControllerBlockEntityTicker(final Supplier<BlockEntityType<ControllerBlockEntity>> allowedTypeSupplier) {
        super(allowedTypeSupplier);
    }

    @Override
    public void tick(final Level level,
                     final BlockPos pos,
                     final BlockState state,
                     final ControllerBlockEntity blockEntity) {
        super.tick(level, pos, state, blockEntity);
        blockEntity.updateEnergyTypeInLevel(state);
    }
}
