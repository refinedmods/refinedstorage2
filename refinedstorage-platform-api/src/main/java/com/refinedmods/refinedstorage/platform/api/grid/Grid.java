package com.refinedmods.refinedstorage.platform.api.grid;

import com.refinedmods.refinedstorage.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage.api.grid.watcher.GridWatcher;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceType;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.0")
public interface Grid {
    void addWatcher(GridWatcher watcher, Class<? extends Actor> actorType);

    void removeWatcher(GridWatcher watcher);

    Storage getItemStorage();

    boolean isGridActive();

    List<TrackedResourceAmount> getResources(Class<? extends Actor> actorType);

    GridOperations createOperations(ResourceType resourceType, ServerPlayer player);
}
