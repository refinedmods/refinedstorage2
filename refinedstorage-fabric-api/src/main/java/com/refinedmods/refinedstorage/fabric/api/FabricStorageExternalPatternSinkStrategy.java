package com.refinedmods.refinedstorage.fabric.api;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collection;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public interface FabricStorageExternalPatternSinkStrategy {
    ExternalPatternSink.Result accept(Transaction tx, Collection<ResourceAmount> resources);

    boolean isEmpty();

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
