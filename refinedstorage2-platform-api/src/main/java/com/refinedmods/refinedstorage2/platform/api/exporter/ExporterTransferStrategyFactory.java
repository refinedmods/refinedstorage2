package com.refinedmods.refinedstorage2.platform.api.exporter;

import com.refinedmods.refinedstorage2.api.network.node.exporter.ExporterTransferStrategy;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeState;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
@FunctionalInterface
public interface ExporterTransferStrategyFactory {
    ExporterTransferStrategy create(
        ServerLevel level,
        BlockPos pos,
        Direction direction,
        UpgradeState upgradeState,
        AmountOverride amountOverride,
        boolean fuzzyMode
    );
}
