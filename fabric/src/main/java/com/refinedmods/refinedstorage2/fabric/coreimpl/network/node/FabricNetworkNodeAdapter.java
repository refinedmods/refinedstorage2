package com.refinedmods.refinedstorage2.fabric.coreimpl.network.node;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeAdapter;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class FabricNetworkNodeAdapter implements NetworkNodeAdapter {
    private final World world;

    public FabricNetworkNodeAdapter(World world) {
        this.world = world;
    }

    @Override
    public Optional<NetworkNode> getNode(BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof NetworkNode) {
            return Optional.of((NetworkNode) blockEntity);
        }

        return Optional.empty();
    }
}
