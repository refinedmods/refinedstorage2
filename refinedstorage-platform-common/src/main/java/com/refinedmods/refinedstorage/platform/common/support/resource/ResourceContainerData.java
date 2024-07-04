package com.refinedmods.refinedstorage.platform.common.support.resource;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ResourceContainerData(List<Optional<ResourceAmount>> resources) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ResourceContainerData> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ResourceCodecs.AMOUNT_STREAM_OPTIONAL_CODEC),
            ResourceContainerData::resources,
            ResourceContainerData::new
        );

    public static ResourceContainerData of(final ResourceContainer resourceContainer) {
        final List<Optional<ResourceAmount>> resources = new ArrayList<>();
        for (int i = 0; i < resourceContainer.size(); ++i) {
            resources.add(Optional.ofNullable(resourceContainer.get(i)));
        }
        return new ResourceContainerData(resources);
    }
}
