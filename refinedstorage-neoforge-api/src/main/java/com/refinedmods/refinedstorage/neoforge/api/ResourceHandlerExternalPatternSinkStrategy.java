package com.refinedmods.refinedstorage.neoforge.api;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "3.0.0")
public interface ResourceHandlerExternalPatternSinkStrategy {
    ExternalPatternSink.Result insert(Transaction tx, ResourceAmount resourceAmount);

    boolean isEmpty();
}
