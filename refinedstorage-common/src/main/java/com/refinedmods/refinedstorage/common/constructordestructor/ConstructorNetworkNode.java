package com.refinedmods.refinedstorage.common.constructordestructor;

import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage.api.network.node.SchedulingMode;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.api.constructordestructor.ConstructorStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public class ConstructorNetworkNode extends SimpleNetworkNode {
    private final Actor actor = new NetworkNodeActor(this);
    private final List<ConstructorTask> tasks = new ArrayList<>();

    @Nullable
    private Supplier<Player> playerProvider;
    @Nullable
    private ConstructorStrategy strategy;
    @Nullable
    private SchedulingMode schedulingMode;

    ConstructorNetworkNode(final long energyUsage) {
        super(energyUsage);
    }

    @Override
    public void doWork() {
        super.doWork();
        if (network == null || !isActive() || schedulingMode == null) {
            return;
        }
        schedulingMode.execute(tasks);
    }

    void setPlayerProvider(@Nullable final Supplier<Player> playerSupplier) {
        this.playerProvider = playerSupplier;
    }

    void setSchedulingMode(@Nullable final SchedulingMode schedulingMode) {
        this.schedulingMode = schedulingMode;
    }

    void setFilters(final List<ResourceKey> filters) {
        final List<ConstructorTask> updatedTasks = new ArrayList<>();
        for (int i = 0; i < filters.size(); ++i) {
            final ResourceKey filter = filters.get(i);
            final ConstructorStrategy.Result lastResult = (i < tasks.size() && tasks.get(i).filter.equals(filter))
                ? tasks.get(i).lastResult
                : null;
            final ConstructorTask task = new ConstructorTask(filter, lastResult);
            updatedTasks.add(task);
        }
        tasks.clear();
        tasks.addAll(updatedTasks);
    }

    void setStrategy(@Nullable final ConstructorStrategy strategy) {
        this.strategy = strategy;
    }

    public ConstructorStrategy.@Nullable Result getLastResult(final int filterIndex) {
        return tasks.get(filterIndex).lastResult;
    }

    private class ConstructorTask implements SchedulingMode.ScheduledTask {
        private final ResourceKey filter;
        private ConstructorStrategy.@Nullable Result lastResult;

        private ConstructorTask(final ResourceKey filter, final ConstructorStrategy.@Nullable Result lastResult) {
            this.filter = filter;
            this.lastResult = lastResult;
        }

        @Override
        public boolean run() {
            if (strategy == null || network == null || playerProvider == null) {
                return false;
            }
            final Player player = playerProvider.get();
            this.lastResult = strategy.apply(filter, actor, player, network);
            return lastResult == ConstructorStrategy.Result.SUCCESS;
        }
    }
}
