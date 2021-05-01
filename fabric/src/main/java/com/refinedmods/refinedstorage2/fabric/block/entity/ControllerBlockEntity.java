package com.refinedmods.refinedstorage2.fabric.block.entity;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.block.ControllerBlock;
import com.refinedmods.refinedstorage2.fabric.block.ControllerEnergyType;
import com.refinedmods.refinedstorage2.fabric.coreimpl.adapter.FabricRs2WorldAdapter;
import com.refinedmods.refinedstorage2.fabric.coreimpl.network.node.FabricNetworkNodeReference;
import com.refinedmods.refinedstorage2.fabric.util.Positions;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ControllerBlockEntity extends NetworkNodeBlockEntity<NetworkNodeImpl> {
    public ControllerBlockEntity() {
        super(Rs2Mod.BLOCK_ENTITIES.getController());
    }

    private int ticks = 0;
    private ControllerEnergyType type = ControllerEnergyType.OFF;

    @Override
    public void tick() {
        super.tick();

        if (world != null && !world.isClient()) {
            ++ticks;

            if (ticks % 20 == 0) {
                type = next(type);
                world.setBlockState(pos, world.getBlockState(pos).with(ControllerBlock.ENERGY_TYPE, type));
            }
        }
    }

    private ControllerEnergyType next(ControllerEnergyType current) {
        switch (current) {
            case OFF:
                return ControllerEnergyType.NEARLY_ON;
            case NEARLY_OFF:
                return ControllerEnergyType.OFF;
            case NEARLY_ON:
                return ControllerEnergyType.ON;
            case ON:
                return ControllerEnergyType.NEARLY_OFF;
        }
        return ControllerEnergyType.OFF;
    }

    @Override
    protected NetworkNodeImpl createNode(World world, BlockPos pos) {
        return new NetworkNodeImpl(FabricRs2WorldAdapter.of(world), Positions.ofBlockPos(pos), FabricNetworkNodeReference.of(world, pos));
    }
}
