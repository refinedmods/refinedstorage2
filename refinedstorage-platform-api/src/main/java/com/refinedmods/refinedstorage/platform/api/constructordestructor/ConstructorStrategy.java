package com.refinedmods.refinedstorage.platform.api.constructordestructor;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;

import net.minecraft.world.entity.player.Player;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.11")
@FunctionalInterface
public interface ConstructorStrategy {
    boolean apply(ResourceKey resource, Actor actor, Player actingPlayer, Network network);
}
