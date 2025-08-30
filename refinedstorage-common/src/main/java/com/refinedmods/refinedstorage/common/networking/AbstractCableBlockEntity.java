package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.support.AbstractCableLikeBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractCableBlockEntity extends AbstractCableLikeBlockEntity<SimpleNetworkNode> {
    protected AbstractCableBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getCable(), pos, state, new SimpleNetworkNode(
            Platform.INSTANCE.getConfig().getCable().getEnergyUsage()));
    }

    @Override
    public Component getName() {
        return overrideName(ContentNames.CABLE);
    }

    @Override
    protected boolean hasRedstoneMode() {
        return false;
    }
}
