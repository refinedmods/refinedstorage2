package com.refinedmods.refinedstorage2.platform.common.storagemonitor;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.platform.api.storagemonitor.StorageMonitorInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;

public class ItemStorageMonitorInsertionStrategy implements StorageMonitorInsertionStrategy {
    @Override
    public Optional<ItemStack> insert(final ResourceKey configuredResource,
                                      final ItemStack stack,
                                      final Actor actor,
                                      final Network network) {
        if (!(configuredResource instanceof ItemResource configuredItemResource)) {
            return Optional.empty();
        }
        final ItemResource resource = ItemResource.ofItemStack(stack);
        if (!configuredItemResource.equals(resource)) {
            return Optional.empty();
        }
        final long inserted = network.getComponent(StorageNetworkComponent.class)
            .getStorageChannel(StorageChannelTypes.ITEM)
            .insert(resource, stack.getCount(), Action.EXECUTE, actor);
        final long remainder = stack.getCount() - inserted;
        if (remainder > 0) {
            return Optional.of(resource.toItemStack(remainder));
        }
        return Optional.of(ItemStack.EMPTY);
    }
}
