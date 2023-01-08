package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage2.platform.api.network.node.PlatformNetworkNodeContainer;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

public class CableBlockEntity extends AbstractInternalNetworkNodeContainerBlockEntity<SimpleNetworkNode> {
    private final DyeColor color;

    public CableBlockEntity(final DyeColor color, final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getCable().get(color),
            pos,
            state,
            new SimpleNetworkNode(Platform.INSTANCE.getConfig().getCable().getEnergyUsage())
        );
        this.color = color;
    }

    @Override
    public boolean canConnectTo(PlatformNetworkNodeContainer other) {
        if (other instanceof CableBlockEntity otherCable) {
            return this.color == otherCable.color && super.canConnectTo(other);
        }
        return super.canConnectTo(other);
    }

    public DyeColor getColor() {
        return this.color;
    }
}
