package com.refinedmods.refinedstorage2.core.network.node;

import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class HidingNetworkNodeRepository implements NetworkNodeRepository {
    private final NetworkNodeRepository parent;
    private final BlockPos hiddenPos;

    public HidingNetworkNodeRepository(NetworkNodeRepository parent, BlockPos hiddenPos) {
        this.parent = parent;
        this.hiddenPos = hiddenPos;
    }

    @Override
    public Optional<NetworkNode> getNode(BlockPos pos) {
        if (hiddenPos.equals(pos)) {
            return Optional.empty();
        }

        return parent.getNode(pos);
    }
}
