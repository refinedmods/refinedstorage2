package com.refinedmods.refinedstorage2.api.network.impl.node.task;

import com.refinedmods.refinedstorage2.api.network.node.task.Task;
import com.refinedmods.refinedstorage2.api.network.node.task.TaskExecutor;

import java.util.ArrayList;
import java.util.List;

public class RandomTaskExecutor<C> implements TaskExecutor<C> {
    private final Randomizer<C> randomizer;

    public RandomTaskExecutor(final Randomizer<C> randomizer) {
        this.randomizer = randomizer;
    }

    @Override
    public void execute(final List<? extends Task<C>> tasks, final C context) {
        if (tasks.isEmpty()) {
            return;
        }
        final List<Task<C>> shuffledTasks = new ArrayList<>(tasks);
        randomizer.shuffle(shuffledTasks);
        for (final Task<C> task : shuffledTasks) {
            if (task.run(context)) {
                return;
            }
        }
    }

    public interface Randomizer<C> {
        void shuffle(List<Task<C>> list);
    }
}
