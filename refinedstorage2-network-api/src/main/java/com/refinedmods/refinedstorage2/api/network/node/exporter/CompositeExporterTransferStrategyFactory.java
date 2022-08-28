package com.refinedmods.refinedstorage2.api.network.node.exporter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public class CompositeExporterTransferStrategyFactory implements ExporterTransferStrategyFactory {
    private final List<ExporterTransferStrategyFactory> factories;

    public CompositeExporterTransferStrategyFactory(final List<ExporterTransferStrategyFactory> factories) {
        this.factories = Collections.unmodifiableList(factories);
    }

    @Override
    public Optional<ExporterTransferStrategy> create(final Object resource) {
        return factories.stream()
            .flatMap(f -> f.create(resource).stream())
            .findFirst();
    }
}
