package com.refinedmods.refinedstorage2.platform.common.block.entity.constructor;

import com.refinedmods.refinedstorage2.platform.api.blockentity.constructor.ConstructorStrategy;
import com.refinedmods.refinedstorage2.platform.api.blockentity.constructor.ConstructorStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeState;
import com.refinedmods.refinedstorage2.platform.common.content.Items;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class ItemDropConstructorStrategyFactory implements ConstructorStrategyFactory {
    @Override
    public Optional<ConstructorStrategy> create(final ServerLevel level,
                                                final BlockPos pos,
                                                final Direction direction,
                                                final UpgradeState upgradeState,
                                                final boolean dropItems) {
        if (!dropItems) {
            return Optional.empty();
        }
        return Optional.of(new ItemDropConstructorStrategy(
            level,
            pos,
            direction,
            upgradeState.hasUpgrade(Items.INSTANCE.getStackUpgrade())
        ));
    }

    @Override
    public int getPriority() {
        return -1;
    }
}
