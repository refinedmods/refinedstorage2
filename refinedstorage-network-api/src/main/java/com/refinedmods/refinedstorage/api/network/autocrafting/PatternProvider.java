package com.refinedmods.refinedstorage.api.network.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.autocrafting.task.StepBehavior;
import com.refinedmods.refinedstorage.api.autocrafting.task.Task;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.List;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.8")
public interface PatternProvider extends ExternalPatternSink, StepBehavior {
    void onAddedIntoContainer(ParentContainer parentContainer);

    void onRemovedFromContainer(ParentContainer parentContainer);

    default boolean contains(final AutocraftingNetworkComponent component) {
        return false;
    }

    void addTask(Task task);

    void cancelTask(TaskId taskId);

    List<TaskStatus> getTaskStatuses();

    long getAmount(ResourceKey resource);

    void receivedExternalIteration();
}
