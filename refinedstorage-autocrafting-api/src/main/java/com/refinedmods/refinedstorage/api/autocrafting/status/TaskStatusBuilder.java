package com.refinedmods.refinedstorage.api.autocrafting.status;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkKey;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskState;
import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

public class TaskStatusBuilder {
    private final TaskStatus.TaskInfo info;
    private final TaskState state;
    private final Map<ResourceKey, MutableItem> items = new LinkedHashMap<>();

    public TaskStatusBuilder(final TaskId id, final TaskState state, final ResourceKey resource, final long amount,
                             final long startTime) {
        this.info = new TaskStatus.TaskInfo(id, resource, amount, startTime);
        this.state = state;
    }

    public void stored(final ResourceKey resource, final long stored) {
        CoreValidations.validateLargerThanZero(stored, "Stored");
        get(resource).stored += stored;
    }

    public void extracting(final ResourceKey resource, final long extracting) {
        CoreValidations.validateLargerThanZero(extracting, "Extracting");
        get(resource).extracting += extracting;
    }

    public void processing(final ResourceKey resource,
                           final long processing,
                           @Nullable final ExternalPatternSinkKey sinkKey) {
        CoreValidations.validateLargerThanZero(processing, "Processing");
        get(resource).processing += processing;
        get(resource).sinkKey = sinkKey;
    }

    public void scheduled(final ResourceKey resource, final long scheduled) {
        CoreValidations.validateLargerThanZero(scheduled, "Crafting");
        get(resource).scheduled += scheduled;
    }

    public void crafting(final ResourceKey resource, final long crafting) {
        CoreValidations.validateLargerThanZero(crafting, "Crafting");
        get(resource).crafting += crafting;
    }

    public void rejected(final ResourceKey resource) {
        get(resource).type = TaskStatus.ItemType.REJECTED;
    }

    public void noneFound(final ResourceKey resource) {
        get(resource).type = TaskStatus.ItemType.NONE_FOUND;
    }

    public void locked(final ResourceKey resource) {
        get(resource).type = TaskStatus.ItemType.LOCKED;
    }

    private MutableItem get(final ResourceKey resource) {
        return items.computeIfAbsent(resource, key -> new MutableItem(TaskStatus.ItemType.NORMAL));
    }

    public TaskStatus build(final double percentageCompleted) {
        final List<TaskStatus.Item> mappedItems = items.entrySet().stream().map(entry -> new TaskStatus.Item(
            entry.getKey(),
            entry.getValue().type,
            entry.getValue().sinkKey,
            entry.getValue().stored,
            entry.getValue().extracting,
            entry.getValue().processing,
            entry.getValue().scheduled,
            entry.getValue().crafting
        )).toList();
        return new TaskStatus(info, state, percentageCompleted, mappedItems);
    }

    private static class MutableItem {
        private TaskStatus.ItemType type;
        private long stored;
        private long extracting;
        private long processing;
        @Nullable
        private ExternalPatternSinkKey sinkKey;
        private long scheduled;
        private long crafting;

        private MutableItem(final TaskStatus.ItemType type) {
            this.type = type;
        }
    }
}
