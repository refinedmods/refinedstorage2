package com.refinedmods.refinedstorage2.platform.api.blockentity.storagemonitor;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.1")
@FunctionalInterface
public interface StorageMonitorInsertionStrategy {
    Optional<ItemStack> insert(Object configuredResource, ItemStack stack, Actor actor, Network network);
}
