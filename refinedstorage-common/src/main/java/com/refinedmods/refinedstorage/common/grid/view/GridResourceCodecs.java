package com.refinedmods.refinedstorage.common.grid.view;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

class GridResourceCodecs {
    static final MapCodec<GridResource> ITEM = RecordCodecBuilder.mapCodec(instance ->
        instance.group(ResourceCodecs.ITEM_CODEC
            .fieldOf("item")
            .forGetter(g -> ((ItemGridResource) g).getItemResource())
        ).apply(instance, itemResource -> RefinedStorageApi.INSTANCE.getGridResourceRepositoryMapper()
            .apply(itemResource)));

    static final MapCodec<GridResource> FLUID = RecordCodecBuilder.mapCodec(instance ->
        instance.group(ResourceCodecs.FLUID_CODEC
            .fieldOf("fluid")
            .forGetter(g -> ((FluidGridResource) g).getFluidResource())
        ).apply(instance, fluidResource -> RefinedStorageApi.INSTANCE.getGridResourceRepositoryMapper()
            .apply(fluidResource)));

    private GridResourceCodecs() {
    }
}
