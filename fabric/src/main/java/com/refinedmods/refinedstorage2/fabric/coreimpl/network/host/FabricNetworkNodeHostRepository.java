package com.refinedmods.refinedstorage2.fabric.coreimpl.network.host;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.host.NetworkNodeHost;
import com.refinedmods.refinedstorage2.core.network.host.NetworkNodeHostRepository;
import com.refinedmods.refinedstorage2.core.util.Position;
import com.refinedmods.refinedstorage2.fabric.util.Positions;

import java.util.Optional;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;

public class FabricNetworkNodeHostRepository implements NetworkNodeHostRepository {
    private final World world;

    public FabricNetworkNodeHostRepository(World world) {
        this.world = world;
    }

    @Override
    public Optional<NetworkNodeHost> getHost(Rs2World world, Position position) {
        // TODO: Use world param.
        BlockEntity blockEntity = this.world.getBlockEntity(Positions.toBlockPos(position));
        if (blockEntity instanceof NetworkNodeHost) {
            return Optional.of((NetworkNodeHost) blockEntity);
        }

        return Optional.empty();
    }
}
