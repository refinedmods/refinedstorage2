package com.refinedmods.refinedstorage.common.api.grid;

import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewProvider;
import com.refinedmods.refinedstorage.api.network.node.grid.GridOperations;
import com.refinedmods.refinedstorage.api.network.node.grid.GridWatcher;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceType;

import java.util.List;
import java.util.Set;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.0")
public interface Grid extends PreviewProvider {
    void addWatcher(GridWatcher watcher, Class<? extends Actor> actorType);

    void removeWatcher(GridWatcher watcher);

    Storage getItemStorage();

    boolean isGridActive();

    List<TrackedResourceAmount> getResources(Class<? extends Actor> actorType);

    Set<PlatformResourceKey> getAutocraftableResources();

    GridOperations createOperations(ResourceType resourceType, ServerPlayer player);

    boolean canMenuStayOpen(Player player);
}
