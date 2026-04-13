package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.storage.TrackedResourceCodecs;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record GridData(boolean active, List<GridResource> resources, Set<PlatformResourceKey> autocraftableResources) {
    public static final StreamCodec<RegistryFriendlyByteBuf, GridData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, GridData::active,
        ByteBufCodecs.collection(ArrayList::new, StreamCodec.composite(
            ResourceCodecs.AMOUNT_STREAM_CODEC, GridResource::resourceAmount,
            TrackedResourceCodecs.OPTIONAL_STREAM_CODEC, GridResource::trackedResource,
            GridResource::new
        )), GridData::resources,
        ByteBufCodecs.collection(HashSet::new, ResourceCodecs.STREAM_CODEC), GridData::autocraftableResources,
        GridData::new
    );

    public static GridData of(final Grid grid) {
        return new GridData(
            grid.isGridActive(),
            grid.getResources(PlayerActor.class).stream().map(GridResource::of).toList(),
            grid.getAutocraftableResources()
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
