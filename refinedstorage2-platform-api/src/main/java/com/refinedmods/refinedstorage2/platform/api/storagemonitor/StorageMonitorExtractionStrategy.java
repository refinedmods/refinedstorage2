package com.refinedmods.refinedstorage2.platform.api.storagemonitor;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import net.minecraft.world.entity.player.Player;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.1")
@FunctionalInterface
public interface StorageMonitorExtractionStrategy {
    boolean extract(Object resource, boolean fullStack, Player player, Actor actor, Network network);
}
