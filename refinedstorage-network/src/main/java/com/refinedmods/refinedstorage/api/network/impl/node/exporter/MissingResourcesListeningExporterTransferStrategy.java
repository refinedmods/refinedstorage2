package com.refinedmods.refinedstorage.api.network.impl.node.exporter;

import com.refinedmods.refinedstorage.api.autocrafting.calculation.TimeoutableCancellationToken;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.node.exporter.ExporterTransferStrategy;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;

import java.util.function.ToLongFunction;

public class MissingResourcesListeningExporterTransferStrategy implements ExporterTransferStrategy {
    private final ExporterTransferStrategy delegate;
    private final OnMissingResources onMissingResources;

    public MissingResourcesListeningExporterTransferStrategy(final ExporterTransferStrategy delegate,
                                                             final OnMissingResources onMissingResources) {
        this.delegate = delegate;
        this.onMissingResources = onMissingResources;
    }

    @Override
    public Result transfer(final ResourceKey resource, final Actor actor, final Network network) {
        final Result result = delegate.transfer(resource, actor, network);
        if (result == Result.RESOURCE_MISSING) {
            return onMissingResources.onMissingResources(resource, actor, network);
        }
        return result;
    }

    @FunctionalInterface
    public interface OnMissingResources {
        Result onMissingResources(ResourceKey resource, Actor actor, Network network);

        static OnMissingResources scheduleAutocrafting(final ToLongFunction<ResourceKey> taskAmountProvider) {
            return (resource, actor, network) -> {
                final AutocraftingNetworkComponent autocrafting = network.getComponent(
                    AutocraftingNetworkComponent.class
                );
                if (!autocrafting.getPatternsByOutput(resource).isEmpty()) {
                    final long amount = taskAmountProvider.applyAsLong(resource);
                    if (amount <= 0) {
                        return Result.DESTINATION_DOES_NOT_ACCEPT;
                    }
                    final var ensureResult = autocrafting.ensureTask(resource, amount, actor,
                        new TimeoutableCancellationToken());
                    final boolean success = ensureResult == AutocraftingNetworkComponent.EnsureResult.TASK_CREATED
                        || ensureResult == AutocraftingNetworkComponent.EnsureResult.TASK_ALREADY_RUNNING;
                    return success ? Result.AUTOCRAFTING_STARTED : Result.AUTOCRAFTING_MISSING_RESOURCES;
                }
                return Result.RESOURCE_MISSING;
            };
        }
    }
}
