package com.refinedmods.refinedstorage2.platform.fabric.block.entity.ticker;

import com.refinedmods.refinedstorage2.platform.fabric.api.block.entity.ticker.NetworkNodeContainerBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.FabricNetworkNodeContainerBlockEntity;

import net.minecraft.block.BlockState;

public class FabricNetworkNodeContainerBlockEntityTicker<T extends FabricNetworkNodeContainerBlockEntity<?>> extends NetworkNodeContainerBlockEntityTicker<T> {
    @Override
    protected void performContainerUpdate(T blockEntity, BlockState state) {
        super.performContainerUpdate(blockEntity, state);
        blockEntity.updateActiveness(state);
    }
}
