package com.refinedmods.refinedstorage.common.api.grid.view;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepositoryMapper;

import com.mojang.serialization.MapCodec;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "3.0.6")
public interface GridResourceType extends ResourceRepositoryMapper<GridResource> {
    MapCodec<GridResource> getMapCodec();

    Class<? extends ResourceKey> getResourceKeyClass();
}
