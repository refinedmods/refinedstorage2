package com.refinedmods.refinedstorage.common.api.constructordestructor;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.resource.filter.Filter;
import com.refinedmods.refinedstorage.api.storage.Actor;

import java.util.function.Supplier;

import net.minecraft.world.entity.player.Player;
import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.10")
@FunctionalInterface
public interface DestructorStrategy {
    boolean apply(Filter filter, Actor actor, Supplier<@Nullable Network> networkProvider, Player player);
}
