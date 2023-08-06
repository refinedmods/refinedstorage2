package com.refinedmods.refinedstorage2.api.network.node.task;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.11")
@FunctionalInterface
public interface Task<C> {
    boolean run(C context);
}
