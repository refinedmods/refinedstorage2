package com.refinedmods.refinedstorage2.platform.common.constructordestructor;

import com.refinedmods.refinedstorage2.api.core.filter.Filter;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.platform.api.constructordestructor.DestructorStrategy;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.world.entity.player.Player;

class CompositeDestructorStrategy implements DestructorStrategy {
    private final List<DestructorStrategy> strategies;

    CompositeDestructorStrategy(final List<DestructorStrategy> strategies) {
        this.strategies = Collections.unmodifiableList(strategies);
    }

    @Override
    public boolean apply(final Filter filter,
                         final Actor actor,
                         final Supplier<Network> networkSupplier,
                         final Player actingPlayer) {
        for (final DestructorStrategy strategy : strategies) {
            if (strategy.apply(filter, actor, networkSupplier, actingPlayer)) {
                return true;
            }
        }
        return false;
    }
}
