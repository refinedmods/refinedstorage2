package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage2.platform.api.network.node.PlatformNetworkNodeContainer;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.CableBlock;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

public class CableBlockEntity extends AbstractInternalNetworkNodeContainerBlockEntity<SimpleNetworkNode> {

    public CableBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getCable(),
            pos,
            state,
            new SimpleNetworkNode(Platform.INSTANCE.getConfig().getCable().getEnergyUsage())
        );
    }

    @Override
    public boolean canAcceptIncomingConnection(final Direction direction, final PlatformNetworkNodeContainer other) {
        if (other instanceof CableBlockEntity otherCable) {
            return colorsAllowConnecting(otherCable) && super.canAcceptIncomingConnection(direction, other);
        }
        return super.canAcceptIncomingConnection(direction, other);
    }

    private boolean colorsAllowConnecting(final CableBlockEntity otherCable) {
        final DyeColor defaultColor = Blocks.INSTANCE.getCable().getDefault().getColor();
        return this.getColor() == otherCable.getColor()
                || this.getColor() == defaultColor
                || otherCable.getColor() == defaultColor;
    }

    private DyeColor getColor() {
        if (this.getBlockState().getBlock() instanceof CableBlock cableBlock) {
            return cableBlock.getColor();
        }
        return Blocks.INSTANCE.getCable().getDefault().getColor();
    }
}
