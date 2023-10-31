package com.refinedmods.refinedstorage2.platform.api.constructordestructor;

import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeState;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.10")
@FunctionalInterface
public interface DestructorStrategyFactory {
    Optional<DestructorStrategy> create(ServerLevel level,
                                        BlockPos pos,
                                        Direction direction,
                                        UpgradeState upgradeState,
                                        boolean pickupItems);

    default int getPriority() {
        return 0;
    }
}
