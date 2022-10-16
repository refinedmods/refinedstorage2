package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.network.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ExternalStorageBlockEntity extends AbstractInternalNetworkNodeContainerBlockEntity<SimpleNetworkNode> {
    public ExternalStorageBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getExternalStorage(), pos, state, new SimpleNetworkNode(0));
    }
}
