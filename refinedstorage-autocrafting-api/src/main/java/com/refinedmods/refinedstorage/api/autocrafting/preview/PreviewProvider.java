package com.refinedmods.refinedstorage.api.autocrafting.preview;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.9")
public interface PreviewProvider {
    CompletableFuture<Optional<Preview>> getPreview(ResourceKey resource, long amount);

    CompletableFuture<Long> getMaxAmount(ResourceKey resource);

    CompletableFuture<Optional<TaskId>> startTask(ResourceKey resource, long amount, Actor actor, boolean notify);

    void cancel();
}
