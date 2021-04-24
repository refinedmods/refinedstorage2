package com.refinedmods.refinedstorage2.fabric.coreimpl.network.node;

import java.util.Optional;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeRepository;
import com.refinedmods.refinedstorage2.core.util.Position;
import com.refinedmods.refinedstorage2.fabric.util.Positions;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;

public class FabricNetworkNodeRepository implements NetworkNodeRepository {
    private final World world;

    public FabricNetworkNodeRepository(World world) {
        this.world = world;
    }

    @Override
    public Optional<NetworkNode> getNode(Position pos) {
        BlockEntity blockEntity = world.getBlockEntity(Positions.toBlockPos(pos));
        if (blockEntity instanceof NetworkNode) {
            return Optional.of((NetworkNode) blockEntity);
        }

        return Optional.empty();
    }
}
