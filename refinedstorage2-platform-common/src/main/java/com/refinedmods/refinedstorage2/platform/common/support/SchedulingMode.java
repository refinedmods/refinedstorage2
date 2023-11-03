package com.refinedmods.refinedstorage2.platform.common.support;

import com.refinedmods.refinedstorage2.api.network.node.task.TaskExecutor;

import java.util.Collections;
import java.util.Random;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;

public final class SchedulingMode<C> {
    private static final String TAG_SCHEDULING_MODE = "sm";

    private SchedulingModeType type;
    @Nullable
    private TaskExecutor<C> taskExecutor;

    private final Runnable listener;
    private final Consumer<TaskExecutor<C>> taskExecutorAcceptor;

    public SchedulingMode(final Runnable listener, final Consumer<TaskExecutor<C>> taskExecutorAcceptor) {
        this.listener = listener;
        this.taskExecutorAcceptor = taskExecutorAcceptor;
        this.type = SchedulingModeType.DEFAULT;
        this.setTaskExecutor(createTaskExecutor(null, type, listener));
    }

    public SchedulingModeType getType() {
        return type;
    }

    public void setType(final SchedulingModeType type) {
        setType(null, type);
        listener.run();
    }

    private void setType(@Nullable final CompoundTag tag, final SchedulingModeType newType) {
        this.type = newType;
        this.setTaskExecutor(createTaskExecutor(tag, newType, listener));
    }

    private void setTaskExecutor(final TaskExecutor<C> taskExecutor) {
        this.taskExecutor = taskExecutor;
        taskExecutorAcceptor.accept(taskExecutor);
    }

    private static <C> TaskExecutor<C> createTaskExecutor(
        @Nullable final CompoundTag tag,
        final SchedulingModeType type,
        final Runnable listener
    ) {
        return type.createTaskExecutor(tag, list -> Collections.shuffle(list, new Random()), listener);
    }

    public void load(final CompoundTag tag) {
        if (tag.contains(TAG_SCHEDULING_MODE)) {
            setType(tag, SchedulingModeType.getById(tag.getInt(TAG_SCHEDULING_MODE)));
        }
    }

    public void writeToTag(final CompoundTag tag) {
        tag.putInt(TAG_SCHEDULING_MODE, type.getId());
        if (taskExecutor != null) {
            type.writeToTag(tag, taskExecutor);
        }
    }
}
