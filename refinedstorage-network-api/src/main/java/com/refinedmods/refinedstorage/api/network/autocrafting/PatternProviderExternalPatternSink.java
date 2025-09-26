package com.refinedmods.refinedstorage.api.network.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collection;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public interface PatternProviderExternalPatternSink {
    ExternalPatternSink.Result accept(Collection<ResourceAmount> resources, Action action);

    /**
     * Used to determine the ordering of sinks when inserting multiple resource types.
     *
     * @param resource the resource
     * @return true if this sink can accept the given resource type
     */
    default boolean applies(ResourceKey resource) {
        return true;
    }
}
