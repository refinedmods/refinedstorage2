package com.refinedmods.refinedstorage2.platform.api.blockentity.constructor;

import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeState;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.11")
@FunctionalInterface
public interface ConstructorStrategyFactory {
    Optional<ConstructorStrategy> create(ServerLevel level,
                                         BlockPos pos,
                                         Direction direction,
                                         UpgradeState upgradeState,
                                         boolean dropItems);

    default int getPriority() {
        return 0;
    }
}
