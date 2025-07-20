package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class PendingAutocraftingRequests {
    private final Set<CompletableFuture<Optional<Preview>>> previews = new HashSet<>();
    private final Set<CompletableFuture<Long>> maxAmounts = new HashSet<>();
    private final Set<CompletableFuture<Optional<TaskId>>> tasks = new HashSet<>();

    public void addPreviewRequest(final CompletableFuture<Optional<Preview>> previewRequest) {
        previews.add(previewRequest);
        previewRequest.whenComplete((p, e) -> previews.remove(previewRequest));
    }

    public void addMaxAmountRequest(final CompletableFuture<Long> maxAmountRequest) {
        maxAmounts.add(maxAmountRequest);
        maxAmountRequest.whenComplete((m, e) -> maxAmounts.remove(maxAmountRequest));
    }

    public void addTaskRequest(final CompletableFuture<Optional<TaskId>> taskRequest) {
        tasks.add(taskRequest);
        taskRequest.whenComplete((t, e) -> tasks.remove(taskRequest));
    }

    public void cancelAll() {
        for (final CompletableFuture<Optional<Preview>> preview : new HashSet<>(previews)) {
            preview.cancel(true);
        }
        for (final CompletableFuture<Long> maxAmount : new HashSet<>(maxAmounts)) {
            maxAmount.cancel(true);
        }
        for (final CompletableFuture<Optional<TaskId>> task : new HashSet<>(tasks)) {
            task.cancel(true);
        }
    }
}
