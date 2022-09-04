package com.refinedmods.refinedstorage2.api.network.node.exporter;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import java.util.List;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public interface ExporterTransferStrategyExecutor {
    void execute(List<ExporterTransferStrategy> strategies, Network network, Actor actor);

    default void onTemplatesChanged() {
    }
}
