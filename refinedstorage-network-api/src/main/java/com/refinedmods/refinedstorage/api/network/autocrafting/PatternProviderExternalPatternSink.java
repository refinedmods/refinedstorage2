package com.refinedmods.refinedstorage.api.network.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import java.util.Collection;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
@FunctionalInterface
public interface PatternProviderExternalPatternSink {
    ExternalPatternSink.Result insertAll(Collection<ResourceAmount> resources, Action action);
}
