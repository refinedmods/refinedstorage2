package com.refinedmods.refinedstorage2.api.network.node.exporter;

import java.util.Optional;

import org.apiguardian.api.API;

/**
 * Creates an {@link ExporterTransferStrategy} for a given resource.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
@FunctionalInterface
public interface ExporterTransferStrategyFactory {
    /**
     * @param resource the resource
     * @return a transfer strategy, if applicable for the resource
     */
    Optional<ExporterTransferStrategy> create(Object resource);
}
