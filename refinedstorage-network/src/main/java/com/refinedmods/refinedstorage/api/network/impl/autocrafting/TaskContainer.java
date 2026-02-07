package com.refinedmods.refinedstorage.api.network.impl.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSinkProvider;
import com.refinedmods.refinedstorage.api.autocrafting.task.StepBehavior;
import com.refinedmods.refinedstorage.api.autocrafting.task.Task;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskListener;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskState;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.autocrafting.ParentContainer;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProvider;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskContainer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskContainer.class);

    private final PatternProvider patternProvider;
    private final List<Task> tasks = new CopyOnWriteArrayList<>();
    private final List<Task> tasksView = Collections.unmodifiableList(tasks);
    private final Set<ParentContainer> parents = new HashSet<>();

    public TaskContainer(final PatternProvider patternProvider) {
        this.patternProvider = patternProvider;
    }

    public List<Task> getAll() {
        return tasksView;
    }

    public List<TaskStatus> getStatuses() {
        return tasks.stream().map(Task::getStatus).toList();
    }

    public void onRemovedFromContainer(final ParentContainer parent) {
        tasks.forEach(parent::taskRemoved);
        parents.remove(parent);
    }

    public void onAddedIntoContainer(final ParentContainer parent) {
        tasks.forEach(task -> parent.taskAdded(patternProvider, task));
        parents.add(parent);
    }

    public void add(final Task task, @Nullable final Network network) {
        tasks.add(task);
        if (network != null) {
            attach(task, network.getComponent(StorageNetworkComponent.class));
        }
    }

    public void cancel(final TaskId id) {
        for (final Task task : tasks) {
            if (task.getId().equals(id)) {
                task.cancel();
                return;
            }
        }
        throw new IllegalArgumentException("Task %s not found".formatted(id));
    }

    public void attachAll(final Network network) {
        final StorageNetworkComponent storage = network.getComponent(StorageNetworkComponent.class);
        tasks.forEach(task -> attach(task, storage));
    }

    public void detachAll(final Network network) {
        final StorageNetworkComponent storage = network.getComponent(StorageNetworkComponent.class);
        tasks.forEach(task -> detach(task, storage));
    }

    private void attach(final Task task, final StorageNetworkComponent storage) {
        storage.addListener(task);
    }

    private void detach(final Task task, final StorageNetworkComponent storage) {
        storage.removeListener(task);
    }

    public void step(final Network network, final StepBehavior stepBehavior, final TaskListener listener) {
        final StorageNetworkComponent storage = network.getComponent(StorageNetworkComponent.class);
        final ExternalPatternSinkProvider sinkProvider = network.getComponent(AutocraftingNetworkComponent.class);
        tasks.removeIf(task -> step(task, storage, sinkProvider, stepBehavior, listener));
    }

    private boolean step(final Task task,
                         final StorageNetworkComponent storage,
                         final ExternalPatternSinkProvider sinkProvider,
                         final StepBehavior stepBehavior,
                         final TaskListener listener) {
        boolean changed;
        boolean completed;
        try {
            changed = task.step(storage, sinkProvider, stepBehavior, listener);
            completed = task.getState() == TaskState.COMPLETED;
        } catch (final Exception e) {
            LOGGER.error("Exception while stepping task {} {}, removing task", task.getResource(), task.getAmount(), e);
            changed = false;
            completed = true;
        }
        if (completed) {
            detach(task, storage);
            parents.forEach(parent -> parent.taskCompleted(task));
        } else if (changed) {
            parents.forEach(parent -> parent.taskChanged(task));
        }
        return completed;
    }

    public long getAmount(final ResourceKey resource) {
        return tasks.stream()
            .filter(task -> task.getResource().equals(resource))
            .mapToLong(Task::getAmount)
            .sum();
    }
}
