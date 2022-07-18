package com.refinedmods.refinedstorage2.platform.api.importer;

import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterTransferStrategy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

@FunctionalInterface
public interface ImporterTransferStrategyFactory {
    ImporterTransferStrategy create(ServerLevel level,
                                    BlockPos pos,
                                    Direction direction,
                                    ImporterNetworkNode networkNode);
}
