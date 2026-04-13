package com.refinedmods.refinedstorage.common.autocrafting.monitor;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;

import org.jspecify.annotations.Nullable;

interface AutocraftingMonitorListener {
    void currentTaskChanged(@Nullable TaskStatus taskStatus);

    void taskAdded(TaskStatus taskStatus);

    void taskRemoved(TaskId taskId);
}
