package com.refinedmods.refinedstorage.common.api.grid.view;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepositoryMapper;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "3.1.0")
public interface GridResourceType extends ResourceRepositoryMapper<GridResource> {
    MapCodec<GridResource> getMapCodec();

    MutableComponent getTitle();

    Identifier getSprite();

    Class<? extends ResourceKey> getResourceType();
}
