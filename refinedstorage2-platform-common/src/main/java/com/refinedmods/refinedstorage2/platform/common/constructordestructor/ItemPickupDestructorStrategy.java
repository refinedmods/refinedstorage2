package com.refinedmods.refinedstorage2.platform.common.constructordestructor;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.filter.Filter;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.platform.api.constructordestructor.DestructorStrategy;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

class ItemPickupDestructorStrategy implements DestructorStrategy {
    private final ServerLevel level;
    private final BlockPos pos;

    ItemPickupDestructorStrategy(final ServerLevel level, final BlockPos pos) {
        this.level = level;
        this.pos = pos;
    }

    @Override
    public boolean apply(final Filter filter,
                         final Actor actor,
                         final Supplier<Network> networkSupplier,
                         final Player actingPlayer) {
        if (!level.isLoaded(pos)) {
            return false;
        }
        final StorageChannel<ItemResource> storageChannel = networkSupplier.get()
            .getComponent(StorageNetworkComponent.class)
            .getStorageChannel(StorageChannelTypes.ITEM);
        final List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos));
        for (final ItemEntity itemEntity : items) {
            tryInsert(filter, actor, storageChannel, itemEntity);
        }
        return true;
    }

    private void tryInsert(final Filter filter,
                           final Actor actor,
                           final StorageChannel<ItemResource> storageChannel,
                           final ItemEntity itemEntity) {
        if (itemEntity.isRemoved()) {
            return;
        }
        final ItemStack itemStack = itemEntity.getItem();
        final ItemResource itemResource = ItemResource.ofItemStack(itemStack);
        if (!filter.isAllowed(itemResource)) {
            return;
        }
        final int amount = itemStack.getCount();
        final long inserted = storageChannel.insert(itemResource, amount, Action.EXECUTE, actor);
        itemStack.shrink((int) inserted);
        if (itemStack.isEmpty()) {
            itemEntity.discard();
        }
    }
}
