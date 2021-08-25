package com.refinedmods.refinedstorage2.platform.fabric.block.entity.ticker;

import com.refinedmods.refinedstorage2.platform.fabric.block.entity.ControllerBlockEntity;

import net.minecraft.block.BlockState;

public class ControllerBlockEntityTicker extends FabricNetworkNodeContainerBlockEntityTicker<ControllerBlockEntity> {
    @Override
    protected void performContainerUpdate(ControllerBlockEntity blockEntity, BlockState state) {
        super.performContainerUpdate(blockEntity, state);
        blockEntity.updateEnergyType(state);
    }
}
