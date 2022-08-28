package com.refinedmods.refinedstorage2.api.network.node.exporter;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.storage.Actor;

import org.apiguardian.api.API;

/**
 * A transfer strategy that transfers a resource from the network to a destination.
 * Created through a {@link ExporterTransferStrategyFactory}, where the resource is given.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public interface ExporterTransferStrategy {
    boolean transfer(Actor actor, Network network);
}
