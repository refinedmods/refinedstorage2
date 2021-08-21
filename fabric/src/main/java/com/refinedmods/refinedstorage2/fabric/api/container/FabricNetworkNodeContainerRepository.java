package com.refinedmods.refinedstorage2.fabric.api.container;

import com.refinedmods.refinedstorage2.api.core.Position;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.core.network.node.container.NetworkNodeContainerRepository;
import com.refinedmods.refinedstorage2.fabric.util.Positions;

import java.util.Optional;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;

public class FabricNetworkNodeContainerRepository implements NetworkNodeContainerRepository {
    private final World world;

    public FabricNetworkNodeContainerRepository(World world) {
        this.world = world;
    }

    @Override
    public <T extends NetworkNode> Optional<NetworkNodeContainer<T>> getContainer(Position position) {
        BlockEntity blockEntity = world.getBlockEntity(Positions.toBlockPos(position));
        if (blockEntity instanceof NetworkNodeContainer<?> networkNodeContainer) {
            return Optional.of((NetworkNodeContainer<T>) networkNodeContainer);
        }
        return Optional.empty();
    }
}
