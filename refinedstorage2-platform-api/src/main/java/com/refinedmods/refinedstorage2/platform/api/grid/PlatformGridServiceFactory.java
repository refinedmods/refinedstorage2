package com.refinedmods.refinedstorage2.platform.api.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.grid.service.GridServiceFactory;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.6")
public interface PlatformGridServiceFactory extends GridServiceFactory {
    GridService<ItemResource> createForItem(Actor actor);

    GridService<FluidResource> createForFluid(Actor actor);
}
