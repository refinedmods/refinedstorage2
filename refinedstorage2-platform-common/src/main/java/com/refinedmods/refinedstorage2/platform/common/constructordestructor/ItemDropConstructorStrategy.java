package com.refinedmods.refinedstorage2.platform.common.constructordestructor;

import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

class ItemDropConstructorStrategy extends AbstractItemConstructorStrategy {
    private final long amount;

    ItemDropConstructorStrategy(
        final ServerLevel level,
        final BlockPos pos,
        final Direction direction,
        final boolean hasStackUpgrade
    ) {
        super(level, pos, direction);
        this.amount = hasStackUpgrade ? 64 : 1;
    }

    @Override
    protected long getTransferAmount() {
        return amount;
    }

    @Override
    protected boolean apply(
        final ItemResource itemResource,
        final ItemStack itemStack,
        final Actor actor,
        final Player actingPlayer
    ) {
        final Vec3 position = new Vec3(
            getDispensePositionX(),
            getDispensePositionY(),
            getDispensePositionZ()
        );
        DefaultDispenseItemBehavior.spawnItem(level, itemStack, 6, direction, position);
        return true;
    }
}
