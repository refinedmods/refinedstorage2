package com.refinedmods.refinedstorage2.api.network.node.task;

import java.util.List;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.11")
@FunctionalInterface
public interface TaskExecutor<C> {
    void execute(List<? extends Task<C>> tasks, C context);
}
