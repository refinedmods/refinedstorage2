package com.refinedmods.refinedstorage.common.api.support.resource;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ResourceContainerContents(List<Optional<ResourceAmount>> slots) {
    public ResourceContainerContents(final List<Optional<ResourceAmount>> slots) {
        this.slots = List.copyOf(slots);
    }

    public static ResourceContainerContents of(final ResourceContainer container) {
        final List<Optional<ResourceAmount>> slots = new ArrayList<>();
        for (int i = 0; i < container.size(); i++) {
            slots.add(Optional.ofNullable(container.get(i)));
        }
        return new ResourceContainerContents(slots);
    }
}
