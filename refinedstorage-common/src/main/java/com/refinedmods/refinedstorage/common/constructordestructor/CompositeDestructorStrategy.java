package com.refinedmods.refinedstorage.common.constructordestructor;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.resource.filter.Filter;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.api.constructordestructor.DestructorStrategy;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

class CompositeDestructorStrategy implements DestructorStrategy {
    private final List<DestructorStrategy> strategies;

    CompositeDestructorStrategy(final List<DestructorStrategy> strategies) {
        this.strategies = Collections.unmodifiableList(strategies);
    }

    @Override
    public boolean apply(final Filter filter,
                         final Actor actor,
                         final Supplier<@Nullable Network> networkProvider,
                         final Player player) {
        for (final DestructorStrategy strategy : strategies) {
            if (strategy.apply(filter, actor, networkProvider, player)) {
                return true;
            }
        }
        return false;
    }
}
