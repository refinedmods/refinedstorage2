package com.refinedmods.refinedstorage2.api.network.impl.node.task;

import com.refinedmods.refinedstorage2.api.network.node.task.Task;
import com.refinedmods.refinedstorage2.api.network.node.task.TaskExecutor;

import java.util.List;

public class RoundRobinTaskExecutor<C> implements TaskExecutor<C> {
    private final State state;

    public RoundRobinTaskExecutor(final State state) {
        this.state = state;
    }

    @Override
    public void execute(final List<? extends Task<C>> tasks, final C context) {
        if (tasks.isEmpty()) {
            return;
        }
        final int startIndex = state.getIndex() % tasks.size();
        for (int i = startIndex; i < tasks.size(); ++i) {
            final Task<C> task = tasks.get(i);
            if (task.run(context)) {
                state.setIndex((state.getIndex() + 1) % tasks.size());
                return;
            }
        }
        state.setIndex(0);
    }

    public int getIndex() {
        return state.getIndex();
    }

    public static class State {
        private final Runnable callback;
        private int index;

        public State(final Runnable callback, final int index) {
            this.index = index;
            this.callback = callback;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(final int index) {
            final boolean didChange = this.index != index;
            this.index = index;
            if (didChange) {
                callback.run();
            }
        }
    }
}
