package com.refinedmods.refinedstorage2.fabric.block.entity;

import com.refinedmods.refinedstorage2.core.network.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage2.core.util.Action;
import com.refinedmods.refinedstorage2.fabric.Rs2Config;
import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.block.ControllerBlock;
import com.refinedmods.refinedstorage2.fabric.block.ControllerEnergyType;
import com.refinedmods.refinedstorage2.fabric.coreimpl.adapter.FabricRs2WorldAdapter;
import com.refinedmods.refinedstorage2.fabric.coreimpl.network.node.FabricNetworkNodeReference;
import com.refinedmods.refinedstorage2.fabric.util.Positions;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ControllerBlockEntity extends NetworkNodeBlockEntity<ControllerNetworkNode> {
    private long lastTypeChanged;
    private ControllerEnergyType lastType = ControllerEnergyType.OFF;

    public ControllerBlockEntity() {
        super(Rs2Mod.BLOCK_ENTITIES.getController());
    }

    @Override
    public void tick() {
        if (world != null && !world.isClient() && node != null) {
            ControllerEnergyType type = ControllerEnergyType.ofState(node.getState());
            if (type != lastType && (lastTypeChanged == 0 || System.currentTimeMillis() - lastTypeChanged > 1000)) {
                this.lastTypeChanged = System.currentTimeMillis();
                this.lastType = type;

                world.setBlockState(pos, world.getBlockState(pos).with(ControllerBlock.ENERGY_TYPE, type));
            }
        }
    }

    @Override
    protected ControllerNetworkNode createNode(World world, BlockPos pos) {
        return new ControllerNetworkNode(
                FabricRs2WorldAdapter.of(world),
                Positions.ofBlockPos(pos),
                FabricNetworkNodeReference.of(world, pos),
                Rs2Config.get().getController().getCapacity()
        );
    }

    public void receive() {
        if (node != null) {
            node.receive(5, Action.EXECUTE);
        }
    }
}
