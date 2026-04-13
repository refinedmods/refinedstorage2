package com.refinedmods.refinedstorage.common.constructordestructor;

import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.Filter;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.api.constructordestructor.DestructorStrategy;

import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public class DestructorNetworkNode extends SimpleNetworkNode {
    private final Actor actor = new NetworkNodeActor(this);
    private final Filter filter = new Filter();

    @Nullable
    private DestructorStrategy strategy;
    @Nullable
    private Supplier<Player> playerProvider;

    DestructorNetworkNode(final long energyUsage) {
        super(energyUsage);
    }

    void setStrategy(@Nullable final DestructorStrategy strategy) {
        this.strategy = strategy;
    }

    void setPlayerProvider(@Nullable final Supplier<Player> playerProvider) {
        this.playerProvider = playerProvider;
    }

    FilterMode getFilterMode() {
        return filter.getMode();
    }

    void setFilterMode(final FilterMode mode) {
        filter.setMode(mode);
    }

    void setFilters(final Set<ResourceKey> filters) {
        filter.setFilters(filters);
    }

    @Override
    public void doWork() {
        super.doWork();
        if (strategy == null || network == null || !isActive() || playerProvider == null) {
            return;
        }
        final Player player = playerProvider.get();
        strategy.apply(filter, actor, this::getNetwork, player);
    }
}
