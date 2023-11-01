package com.refinedmods.refinedstorage2.platform.common.constructordestructor;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.platform.api.constructordestructor.ConstructorStrategy;

import java.util.Collections;
import java.util.List;

import net.minecraft.world.entity.player.Player;

class CompositeConstructorStrategy implements ConstructorStrategy {
    private final List<ConstructorStrategy> strategies;

    CompositeConstructorStrategy(final List<ConstructorStrategy> strategies) {
        this.strategies = Collections.unmodifiableList(strategies);
    }

    @Override
    public boolean apply(final Object resource, final Actor actor, final Player actingPlayer, final Network network) {
        for (final ConstructorStrategy strategy : strategies) {
            if (strategy.apply(resource, actor, actingPlayer, network)) {
                return true;
            }
        }
        return false;
    }
}
