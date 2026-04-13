package com.refinedmods.refinedstorage.api.autocrafting.status;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkKey;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskState;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.List;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.10")
public record TaskStatus(TaskInfo info, TaskState state, double percentageCompleted, List<Item> items) {
    public record TaskInfo(TaskId id, ResourceKey resource, long amount, long startTime) {
    }

    public record Item(
        ResourceKey resource,
        ItemType type,
        @Nullable ExternalPatternSinkKey sinkKey,
        long stored,
        long extracting,
        long processing,
        long scheduled,
        long crafting
    ) {
    }

    public enum ItemType {
        NORMAL,
        REJECTED,
        NONE_FOUND,
        LOCKED
    }
}
