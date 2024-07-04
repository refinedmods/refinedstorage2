package com.refinedmods.refinedstorage.platform.api.storagemonitor;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.1")
@FunctionalInterface
public interface StorageMonitorInsertionStrategy {
    Optional<ItemStack> insert(ResourceKey configuredResource, ItemStack stack, Actor actor, Network network);
}
