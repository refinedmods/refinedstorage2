package com.refinedmods.refinedstorage2.platform.api.blockentity.constructor;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import net.minecraft.world.entity.player.Player;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.11")
@FunctionalInterface
public interface ConstructorStrategy {
    boolean apply(Object resource, Actor actor, Player actingPlayer, Network network);
}
