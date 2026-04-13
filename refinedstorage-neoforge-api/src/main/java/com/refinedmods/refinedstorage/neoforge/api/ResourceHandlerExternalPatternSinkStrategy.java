package com.refinedmods.refinedstorage.neoforge.api;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collection;

import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "3.0.0")
public interface ResourceHandlerExternalPatternSinkStrategy {
    ExternalPatternSink.Result accept(Transaction tx, Collection<ResourceAmount> resources);

    boolean isEmpty();

    /**
     * Used to determine the ordering of sinks when inserting multiple resource types.
     *
     * @param resource the resource
     * @return true if this sink can accept the given resource type
     */
    boolean applies(ResourceKey resource);
}
