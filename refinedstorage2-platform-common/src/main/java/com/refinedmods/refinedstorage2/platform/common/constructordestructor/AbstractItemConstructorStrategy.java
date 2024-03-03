package com.refinedmods.refinedstorage2.platform.common.constructordestructor;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.platform.api.constructordestructor.ConstructorStrategy;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

abstract class AbstractItemConstructorStrategy implements ConstructorStrategy {
    protected final ServerLevel level;
    protected final BlockPos pos;
    protected final Direction direction;

    AbstractItemConstructorStrategy(final ServerLevel level, final BlockPos pos, final Direction direction) {
        this.level = level;
        this.pos = pos;
        this.direction = direction;
    }

    protected long getTransferAmount() {
        return 1;
    }

    @Override
    public final boolean apply(
        final ResourceKey resource,
        final Actor actor,
        final Player actingPlayer,
        final Network network
    ) {
        if (!level.isLoaded(pos)) {
            return false;
        }
        if (!(resource instanceof ItemResource itemResource)) {
            return false;
        }
        final StorageChannel storageChannel = network.getComponent(StorageNetworkComponent.class)
            .getStorageChannel(StorageChannelTypes.ITEM);
        final long amount = getTransferAmount();
        final long extractedAmount = storageChannel.extract(itemResource, amount, Action.SIMULATE, actor);
        if (extractedAmount == 0) {
            return false;
        }
        final ItemStack itemStack = itemResource.toItemStack(extractedAmount);
        final boolean success = apply(itemResource, itemStack, actor, actingPlayer);
        if (success) {
            storageChannel.extract(itemResource, extractedAmount, Action.EXECUTE, actor);
        }
        return success;
    }

    protected abstract boolean apply(
        ItemResource itemResource,
        ItemStack itemStack,
        Actor actor,
        Player actingPlayer
    );

    protected double getDispensePositionX() {
        return pos.getX() + 0.5D;
    }

    protected double getDispensePositionY() {
        return pos.getY() + (direction == Direction.DOWN ? 0.45D : 0.5D);
    }

    protected double getDispensePositionZ() {
        return pos.getZ() + 0.5D;
    }
}
