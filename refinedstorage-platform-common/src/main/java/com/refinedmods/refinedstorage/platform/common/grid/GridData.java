package com.refinedmods.refinedstorage.platform.common.grid;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.platform.api.grid.Grid;
import com.refinedmods.refinedstorage.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.platform.common.storage.StorageCodecs;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record GridData(boolean active, List<GridResource> resources) {
    public static final StreamCodec<RegistryFriendlyByteBuf, GridData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, GridData::active,
        ByteBufCodecs.collection(ArrayList::new, StreamCodec.composite(
            ResourceCodecs.AMOUNT_STREAM_CODEC, GridResource::resourceAmount,
            StorageCodecs.TRACKED_RESOURCE_OPTIONAL_STREAM_CODEC, GridResource::trackedResource,
            GridResource::new
        )), GridData::resources,
        GridData::new
    );

    public static GridData of(final Grid grid) {
        return new GridData(
            grid.isGridActive(),
            grid.getResources(PlayerActor.class).stream().map(GridResource::of).toList()
        );
    }

    public record GridResource(ResourceAmount resourceAmount, Optional<TrackedResource> trackedResource) {
        static GridResource of(final TrackedResourceAmount trackedResourceAmount) {
            return new GridResource(
                trackedResourceAmount.resourceAmount(),
                Optional.ofNullable(trackedResourceAmount.trackedResource())
            );
        }
    }
}
