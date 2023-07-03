package com.refinedmods.refinedstorage2.api.network.impl.node.task;

import com.refinedmods.refinedstorage2.api.network.node.task.Task;
import com.refinedmods.refinedstorage2.api.network.node.task.TaskExecutor;

import java.util.List;

public class DefaultTaskExecutor<C> implements TaskExecutor<C> {
    @Override
    public void execute(final List<? extends Task<C>> tasks, final C context) {
        for (final Task<C> task : tasks) {
            if (task.run(context)) {
                return;
            }
        }
    }
}
