package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutocraftingTasks {
    private final Map<ResourceKey, MergedAutocraftingTask> tasks = new HashMap<>();

    public void addOrUpdateStatus(final TaskStatus status) {
        final ResourceKey resource = status.info().resource();
        tasks.computeIfAbsent(resource, r -> new MergedAutocraftingTask()).addOrUpdateStatus(status);
    }

    public void removeStatus(final TaskId id) {
        tasks.values().removeIf(task -> task.removeStatus(id));
    }

    public Collection<TaskStatus> getStatuses(final ResourceKey resource) {
        final MergedAutocraftingTask task = tasks.get(resource);
        if (task == null) {
            return Collections.emptyList();
        }
        return task.getStatuses();
    }

    public List<TaskStatus.Item> getMergedItems(final ResourceKey resource) {
        final MergedAutocraftingTask task = tasks.get(resource);
        if (task == null) {
            return Collections.emptyList();
        }
        return task.getMergedItems();
    }
}
