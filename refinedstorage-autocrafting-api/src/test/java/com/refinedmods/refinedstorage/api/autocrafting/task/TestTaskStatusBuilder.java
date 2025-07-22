package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatusBuilder;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Exactly the same as {@link TaskStatusBuilder}, but for use in tests.
 * This allows the mutation test to fail on production code, otherwise the mutants
 * would also be applied to the test assertions and there would be no test failure.
 */
public class TestTaskStatusBuilder {
    private final TaskStatus.TaskInfo info;
    private final Map<ResourceKey, MutableItem> items = new LinkedHashMap<>();

    public TestTaskStatusBuilder(final TaskId id, final ResourceKey resource, final long amount, final long startTime) {
        this.info = new TaskStatus.TaskInfo(id, resource, amount, startTime);
    }

    public TestTaskStatusBuilder stored(final ResourceKey resource, final long stored) {
        get(resource).stored += stored;
        return this;
    }

    public TestTaskStatusBuilder processing(final ResourceKey resource,
                                            final long processing,
                                            @Nullable final ExternalPatternSinkKey sinkKey) {
        get(resource).processing += processing;
        get(resource).sinkKey = sinkKey;
        return this;
    }

    public TestTaskStatusBuilder scheduled(final ResourceKey resource, final long scheduled) {
        get(resource).scheduled += scheduled;
        return this;
    }

    public TestTaskStatusBuilder crafting(final ResourceKey resource, final long crafting) {
        get(resource).crafting += crafting;
        return this;
    }

    public TestTaskStatusBuilder rejected(final ResourceKey resource) {
        get(resource).type = TaskStatus.ItemType.REJECTED;
        return this;
    }

    public TestTaskStatusBuilder noneFound(final ResourceKey resource) {
        get(resource).type = TaskStatus.ItemType.NONE_FOUND;
        return this;
    }

    public TestTaskStatusBuilder locked(final ResourceKey resource) {
        get(resource).type = TaskStatus.ItemType.LOCKED;
        return this;
    }

    private MutableItem get(final ResourceKey resource) {
        return items.computeIfAbsent(resource, key -> new MutableItem(TaskStatus.ItemType.NORMAL));
    }

    public TaskStatus build(final double percentageCompleted) {
        return new TaskStatus(info, percentageCompleted, items.entrySet().stream().map(entry -> new TaskStatus.Item(
            entry.getKey(),
            entry.getValue().type,
            entry.getValue().sinkKey,
            entry.getValue().stored,
            entry.getValue().processing,
            entry.getValue().scheduled,
            entry.getValue().crafting
        )).toList());
    }

    private static class MutableItem {
        private TaskStatus.ItemType type;
        private long stored;
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
