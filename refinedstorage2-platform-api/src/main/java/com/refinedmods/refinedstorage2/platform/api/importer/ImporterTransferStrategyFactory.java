package com.refinedmods.refinedstorage2.platform.api.importer;

import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterTransferStrategy;
import com.refinedmods.refinedstorage2.platform.api.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeState;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.1")
@FunctionalInterface
public interface ImporterTransferStrategyFactory {
    ImporterTransferStrategy create(
        ServerLevel level,
        BlockPos pos,
        Direction direction,
        UpgradeState upgradeState,
        AmountOverride amountOverride
    );
}
