package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkDetails;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

class MergedAutocraftingTask {
    private final Map<TaskId, TaskStatus> statuses = new LinkedHashMap<>();
    private final List<TaskStatus.Item> mergedItems = new ArrayList<>();
    private final List<TaskStatus.Item> mergedItemsView = Collections.unmodifiableList(mergedItems);

    void addOrUpdateStatus(final TaskStatus status) {
        statuses.put(status.info().id(), status);
        updateMergedItems();
    }

    boolean removeStatus(final TaskId id) {
        statuses.remove(id);
        updateMergedItems();
        return statuses.isEmpty();
    }

    Collection<TaskStatus> getStatuses() {
        return statuses.values();
    }

    List<TaskStatus.Item> getMergedItems() {
        return mergedItemsView;
    }

    private void updateMergedItems() {
        mergedItems.clear();
        mergedItems.addAll(merge(statuses.values()));
    }

    private static List<TaskStatus.Item> merge(final Collection<TaskStatus> statuses) {
        final Map<ItemMergeKey, MutableMergedItem> grouped = new LinkedHashMap<>();
        for (final TaskStatus status : statuses) {
            for (final TaskStatus.Item item : status.items()) {
                final ItemMergeKey key = new ItemMergeKey(item.resource(), item.type(), item.details());
                final MutableMergedItem accumulator = grouped.computeIfAbsent(key, k -> new MutableMergedItem());
                accumulator.stored += item.stored();
                accumulator.extracting += item.extracting();
                accumulator.processing += item.processing();
                accumulator.scheduled += item.scheduled();
                accumulator.crafting += item.crafting();
            }
        }
        return grouped.entrySet().stream().map(entry -> new TaskStatus.Item(
            entry.getKey().resource,
            entry.getKey().type,
            entry.getKey().details,
            entry.getValue().stored,
            entry.getValue().extracting,
            entry.getValue().processing,
            entry.getValue().scheduled,
            entry.getValue().crafting
        )).toList();
    }

    private record ItemMergeKey(
        ResourceKey resource,
        TaskStatus.ItemType type,
        @Nullable ExternalPatternSinkDetails details
    ) {
    }

    private static final class MutableMergedItem {
        private long stored;
        private long extracting;
        private long processing;
        private long scheduled;
        private long crafting;
    }
}
